package com.gridnine.spf.updater.handlers;

public interface CommandHandler {
    String getCommand();

    byte[] handleCommand(String parameters, byte[] body) throws Exception;

    default boolean isStopCommand(){
        return false;
    }
}
