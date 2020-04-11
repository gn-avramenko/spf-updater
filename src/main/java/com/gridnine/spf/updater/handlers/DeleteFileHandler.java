package com.gridnine.spf.updater.handlers;

import java.io.File;

public class DeleteFileHandler implements CommandHandler{
    @Override
    public String getCommand() {
        return "DELETE_FILE";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {
        File file = new File("data/localrepository/" + parameters);
        if(!file.delete()){
            throw new Exception("unable to delete file " + parameters);
        }
        return "OK".getBytes();
    }
}
