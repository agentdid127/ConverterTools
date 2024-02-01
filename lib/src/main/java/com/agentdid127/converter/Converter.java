package com.agentdid127.converter;

import java.io.IOException;

public abstract class Converter {

    private String name;
    private int priority;

    public Converter(String name, int priority) {
	    this.name = name;
	    this.priority = priority;
    }

    public String getName() {
	return name;
    }

    public int getPriority() {
	return priority;
    }

    public abstract void convert() throws IOException;
}
