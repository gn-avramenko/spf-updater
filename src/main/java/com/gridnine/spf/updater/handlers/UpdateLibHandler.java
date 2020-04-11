package com.gridnine.spf.updater.handlers;

import java.io.File;

public class UpdateLibHandler implements CommandHandler{
    private final File spfLibDir;

    public UpdateLibHandler(File spfLibDir){
        this.spfLibDir = spfLibDir;
    }
    @Override
    public String getCommand() {
        return "UPDATE_LIB";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {
        FileHelper.syncDirs(new File("data/localrepository"), spfLibDir);
        return "OK".getBytes();
    }
}
