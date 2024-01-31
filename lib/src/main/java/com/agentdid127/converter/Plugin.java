package com.agentdid127.converter;

import java.util.ArrayList;
import java.util.List;

public abstract class Plugin {

    protected String name;
    protected List<Converter> converters;
    
    public Plugin(String name) {
	this.name = name;
	this.converters = new ArrayList<>();
    }
    
    public String getName() {
	return name;
    }
    
    public abstract void onLoad();

    public abstract void onUnload();

    public List<Converter> getConverters() {
	return converters;
    }
} // Plugin
