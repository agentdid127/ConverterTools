package com.agentdid127.converter;

import com.agentdid127.converter.iface.Application;
import java.util.ArrayList;
import java.util.List;

public abstract class Plugin {

    protected String name;
    private List<Converter> converters;
    private Application application;

    public Plugin(String name) {
	this.name = name;
	this.converters = new ArrayList<>();
    }
    
    public String getName() {
	return name;
    }
    
    public abstract void onLoad();

    public abstract void onInit();

    public abstract void onUnload();

    public List<Converter> getConverters() {
	return this.converters;
    }

    public void setApplication(Application app) {
      this.application = app;
    }

    public Application getApplication() {
      return application;
    }
} // Plugin
