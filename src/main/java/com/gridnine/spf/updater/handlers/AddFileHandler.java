package com.gridnine.spf.updater.handlers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AddFileHandler implements CommandHandler{
    @Override
    public String getCommand() {
        return "ADD_FILE";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {
        File file = new File("data/localrepository/" + parameters);
        Files.write(Paths.get(file.toURI()), body);
        return "OK".getBytes();
    }
}
