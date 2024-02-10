package com.agentdid127.converter.util;

import java.io.PrintStream;

public class Logger {

    private static org.slf4j.Logger logger = null;

    private static PrintStream stream = null;
    private static PrintStream errorStream = null;


    public static void setLogger(org.slf4j.Logger logger) {
        Logger.logger = logger;
        setStreams(null, null);
    }
    public static void setStream(PrintStream stream) {
        Logger.stream = stream;
    }

    public static void setErrorStream(PrintStream stream) {
	      Logger.errorStream = stream;
    }

    public static void setStreams(PrintStream stream, PrintStream errorStream) {
        setStream(stream);
        setErrorStream(errorStream);
        setLogger(null);
    }

    public static void log(String message) {
        if (logger == null) {
            stream.println(message);
        } else {
         logger.info(message);
        }
    }

    public static void log(Object thing) {
        log(String.valueOf(thing));
    }

    public static void error(String message) {
        if (logger == null) {
            errorStream.println(message);
        } else {
            logger.error(message);
        }
    }

    public static void error(Object thing) {
	      error(String.valueOf(thing));
    }
}
