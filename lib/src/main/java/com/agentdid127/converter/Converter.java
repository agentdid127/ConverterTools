package com.agentdid127.converter;

import java.io.IOException;

public abstract class Converter extends Runner {

    public Converter(String name, int priority) {
	    super(name, priority);
    }

    public abstract void convert() throws IOException;
}
