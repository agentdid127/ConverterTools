package com.agentdid127.converter.core;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import com.agentdid127.converter.Plugin;
import com.agentdid127.converter.Converter;

public class PluginLoader {

    private final Map<String, Plugin> plugins = new HashMap<>();
    private final File pluginsDir;
    private final AtomicBoolean loading = new AtomicBoolean();
    
    public PluginLoader(final File pluginsDir) {
	this.pluginsDir = pluginsDir;
    }
    
    public void loadPlugins() {
	if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
	    System.err.println("Skipping Plugin Loading. Plugin dir not found: " + pluginsDir);
	    return;
	}

	if (loading.compareAndSet(false, true)) {
	    final File[] files = requireNonNull(pluginsDir.listFiles());
	    for (File pluginDir : files) {
		if (pluginDir.isDirectory()) {
		    loadPlugin(pluginDir);
		}
	    }
	}
    }

    public Map<String, Plugin> getPlugins() {
	return plugins;
    }

    private void loadPlugin(final File pluginDir) {
	System.out.println("Loading plugin: " + pluginDir);
	final URLClassLoader pluginClassLoader = createPluginClassLoader(pluginDir);
	final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
	try {
	    Thread.currentThread().setContextClassLoader(pluginClassLoader);
	    ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, pluginClassLoader);
	    for (Plugin plugin : loader) {
		installPlugin(plugin);
	    }
	} finally {
	    Thread.currentThread().setContextClassLoader(currentClassLoader);
	}
    }


    private void installPlugin(Plugin plugin) {
	if (this.plugins.containsKey(plugin.getName())) {
	    System.out.println("Failed to load Plugin: " + plugin.getName());
	    return;
	}
	this.plugins.put(plugin.getName(), plugin);
	plugin.onLoad();
    }

    

    private URLClassLoader createPluginClassLoader(File dir) {
	final URL[] urls = Arrays.stream(Optional.of(dir.listFiles()).orElse(new File[]{}))
	    .sorted()
	    .map(File::toURI)
	    .map(this::toUrl)
	    .toArray(URL[]::new);

	return new PluginClassLoader(urls, getClass().getClassLoader());
    }

    private URL toUrl(final URI uri) {
	try {
	    return uri.toURL();
	} catch (MalformedURLException e) {
	    throw new RuntimeException(e);
	}
    }

    
}
