package com.gridnine.spf.updater.handlers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InitLocalRepositoryHandler implements CommandHandler{
    private final File spfLibDir;

    public InitLocalRepositoryHandler(File spfLibDir){
        this.spfLibDir = spfLibDir;
    }
    @Override
    public String getCommand() {
        return "INIT_LOCAL_REPOSITORY";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {
        File file = new File("data/localrepository");
        if(!file.exists()){
            file.mkdirs();
        }
        FileHelper.syncDirs(spfLibDir, file);

        return "OK".getBytes();
    }


}
