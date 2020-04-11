package com.gridnine.spf.updater.handlers;

import java.io.File;

public class GetExistingFilesInfoHandler implements CommandHandler{
    private final File  libDir;


    public GetExistingFilesInfoHandler(File libDir){
        this.libDir = libDir;
    }


    @Override
    public String getCommand() {
        return "GET_EXISTING_FILES_INFO";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {
        if(!libDir.exists()){
            return "".getBytes();
        }
        StringBuilder sb = new StringBuilder();
        for(File file: libDir.listFiles()){
            if(file.isFile()){
                if(sb.length()>0){
                    sb.append("\r\n");
                }
                sb.append(file.getName()).append("|").append(FileHelper.calculateCheckSum(file));
            }
        }
        return sb.toString().getBytes();
    }

}
