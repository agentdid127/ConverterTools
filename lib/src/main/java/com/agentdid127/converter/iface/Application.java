package com.agentdid127.converter.iface;

public interface Application {

  default String getData() {
    return "";
  };

  IPluginLoader getPluginLoader();

}
