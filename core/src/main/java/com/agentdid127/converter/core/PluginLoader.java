package com.agentdid127.converter.core;

import static java.util.Objects.requireNonNull;

import com.agentdid127.converter.iface.IPluginLoader;
import com.agentdid127.converter.util.Logger;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import com.agentdid127.converter.Plugin;

public class PluginLoader<T extends Plugin> implements IPluginLoader<T> {

	private final Map<String, T> plugins = new HashMap<>();
	private final File pluginsDir;
	private final AtomicBoolean loading = new AtomicBoolean();
	private Class<T> parameterClass;
	private List<String> sharedPackages;

	public PluginLoader(final File pluginsDir, Class<T> parameterClass) {
		this.pluginsDir = pluginsDir;
		this.parameterClass = parameterClass;
		this.sharedPackages = Arrays.asList(
				"com.agentdid127.converter"
		);
	}

	public PluginLoader(final File pluginsDir, Class<T> parameterClass, List<String> sharedPackages) {
		this.pluginsDir = pluginsDir;
		this.parameterClass = parameterClass;
		this.sharedPackages = sharedPackages;
	}

	public void loadPlugins() {
		if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
			Logger.error("Skipping Plugin Loading. Plugin dir not found: " + pluginsDir);
			return;
		}

		if (loading.compareAndSet(false, true)) {
			final File[] files = requireNonNull(pluginsDir.listFiles());
			boolean found = false;
			boolean jars = false;
			for (File pluginDir : files) {
				if (pluginDir.isDirectory()) {
					found = true;
					loadPlugin(pluginDir);
				} else if (pluginDir.getName().endsWith(".jar")) {
					jars = true;
				}
			}
			if (!found && jars) {
				loadPlugin(pluginsDir);
			}
		}
	}

	private void loadPlugin(final File pluginDir) {
		final URLClassLoader pluginClassLoader = createPluginClassLoader(pluginDir);
		final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(pluginClassLoader);
			for (T plugin : ServiceLoader.load(parameterClass, pluginClassLoader)) {
				Logger.log("Loading Plugin: " + plugin.getName());
				installPlugin(plugin);
			}
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}


	private void installPlugin(final T plugin) {
		plugins.put(plugin.getName(), plugin);
		plugin.onLoad();
	}

	private URLClassLoader createPluginClassLoader(File dir) {
		final URL[] urls = Arrays.stream(Optional.of(dir.listFiles()).orElse(new File[]{}))
				.sorted()
				.map(File::toURI)
				.map(this::toUrl)
				.toArray(URL[]::new);

		return new PluginClassLoader(urls, getClass().getClassLoader(), sharedPackages);
	}

	private URL toUrl(final URI uri) {
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, T> getPlugins() {
		return plugins;
	}
}