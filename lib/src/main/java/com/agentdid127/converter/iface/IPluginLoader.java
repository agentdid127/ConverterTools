package com.agentdid127.converter.iface;

import com.agentdid127.converter.Plugin;
import java.util.Map;

public interface IPluginLoader<T extends Plugin> {

  Map<String, T> getPlugins();


}
