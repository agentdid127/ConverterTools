package com.agentdid127.converter.core;

import static java.util.Objects.requireNonNull;

import com.agentdid127.converter.iface.IPluginLoader;
import com.agentdid127.converter.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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

	public void loadPlugins() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
		if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
			Logger.error("Skipping Plugin Loading. Plugin dir not found: " + pluginsDir);
			return;
		}

		ArrayList<JarFile> jarFiles = new ArrayList<>();
		List<String> names = new ArrayList<>();
		List<List<String>> dependencies = new ArrayList<>();

		List<String> pluginPath = new ArrayList<>();

		for (File file : pluginsDir.listFiles()) {
			if (file.getName().endsWith(".jar")) {
				try {
					JarFile jarFile = new JarFile(file.getAbsolutePath());
					jarFiles.add(jarFile);

					Enumeration<? extends ZipEntry> entries = jarFile.entries();

					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();

						if (entry.getName().equals("plugin.properties")) {
							InputStream inputStream = jarFile.getInputStream(entry);
							Properties properties = new Properties();
							properties.load(inputStream);

							names.add(properties.getProperty("pluginName"));
							String deps = properties.getProperty("dependencies");
							pluginPath.add(properties.getProperty("class"));

							String[] depArr = deps.split(",");
							List<String> depList = new ArrayList<>();

							for (String s : depArr) {
								if (!s.isEmpty()) {
									depList.add(s);
								}
							}
							dependencies.add(depList);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

        List<String> toLoad = new ArrayList<>(names);

		List<String> loadOrder = new ArrayList<>();

		int iter = 0;
		while (!toLoad.isEmpty()) {
			if (iter >= toLoad.size()) {
				toLoad.clear();
				break;
			}

			boolean doLoad = true;
			for (int j = 0; j < names.size(); j++) {

				if (toLoad.get(iter).equals(names.get(j))) {
					List<String> deps = dependencies.get(j);
					for (String s : deps) {
						if (!loadOrder.contains(s)) {
							doLoad = false;
							break;
						}
					}
					break;
				}


			}
			if (doLoad) {
				loadOrder.add(toLoad.get(iter));
				toLoad.remove(iter);
				iter = 0;
			} else {
				iter++;
			}
		}


		List<JarFile> loadJars = new ArrayList<>();
		List<String> loadPath = new ArrayList<>();
		for (String s : loadOrder) {
			for (int i = 0; i < names.size(); i++) {
				if (s.equals(names.get(i))) {
					loadJars.add(jarFiles.get(i));
					loadPath.add(pluginPath.get(i));
				}
			}
		}


		URLClassLoader cl = createPluginClassLoader(pluginsDir);

		for (int i = 0; i < loadJars.size(); i++) {
			JarFile jf = loadJars.get(i);
			Enumeration<JarEntry> e = jf.entries();

			while (e.hasMoreElements()) {
				JarEntry je = e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				String className = je.getName().substring(0, je.getName().length()-6);
				className = className.replace('/', '.');
				if (className.equals(loadPath.get(i))) {
					Class<T> c = (Class<T>) cl.loadClass(className);
					for (Constructor<?> constructor : c.getConstructors()) {
						if (constructor.getParameters().length == 0) {
							T plugin = (T) constructor.newInstance();
							installPlugin(plugin);
						}
					}
				} else {
					cl.loadClass(className);
				}
			}
		}
		Thread.currentThread().setContextClassLoader(cl);
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