package com.agentdid127.converter.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

/**
 * Loads a plugin leveraging a {@link URLClassLoader}. However, it restricts the plugin from
 * using the system classloader thereby trimming access to all system classes.
 *
 * Only the classes in SHARED_PACKAGES are visible to the plugin.
 */
public class PluginClassLoader extends URLClassLoader {

  private List<String> sharedPackages;

  private final ClassLoader parentClassLoader;

  public PluginClassLoader(URL[] urls, ClassLoader parentClassLoader, List<String> sharedPackages) {
    super(urls, null);
    this.parentClassLoader = parentClassLoader;
    this.sharedPackages = sharedPackages;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    // has the class loaded already?
    Class<?> loadedClass = findLoadedClass(name);
    if (loadedClass == null) {
      final boolean isSharedClass = sharedPackages.stream().anyMatch(name::startsWith);
      if (isSharedClass) {
        loadedClass = parentClassLoader.loadClass(name);
      } else {
        loadedClass = super.loadClass(name, resolve);
      }
    }

    if (resolve) {      // marked to resolve
      resolveClass(loadedClass);
    }
    return loadedClass;
  }
}