package com.agentdid127.converter;

import com.agentdid127.converter.iface.Application;
import java.util.ArrayList;
import java.util.List;

public abstract class Plugin<T extends Converter> {

    protected String name;
    protected String type;
    private List<T> converters;
    private Application application;

    public Plugin(String name, String type) {
      this.name = name;
      this.type = type;
      this.converters = new ArrayList<>();
    }
    
    public String getName() {
	return name;
    }

    public String getType() {
      return type;
    }
    
    public abstract void onLoad();

    public abstract void onInit();

    public abstract void onUnload();

    public List<T> getConverters() {
	return this.converters;
    }

    public void setApplication(Application app) {
      this.application = app;
    }

    public Application getApplication() {
      return application;
    }
} // Plugin
