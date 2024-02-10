package com.agentdid127.converter;

import java.io.IOException;

public abstract class Runner {

    private String name;
    private int priority;

    public Runner(String name, int priority) {
	    this.name = name;
	    this.priority = priority;
    }

    public String getName() {
	return name;
    }

    public int getPriority() {
	return priority;
    }

}
