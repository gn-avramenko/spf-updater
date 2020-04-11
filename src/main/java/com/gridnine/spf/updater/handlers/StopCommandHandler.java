package com.gridnine.spf.updater.handlers;

public class StopCommandHandler implements CommandHandler {

    private final StopAppCallback callback;

    public StopCommandHandler(StopAppCallback callback){
        this.callback = callback;
    }
    @Override
    public String getCommand() {
        return "STOP";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) {
        this.callback.stopApplication();
        return "OK".getBytes();
    }

    @Override
    public boolean isStopCommand() {
        return true;
    }
}
