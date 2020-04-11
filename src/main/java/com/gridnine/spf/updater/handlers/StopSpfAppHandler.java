package com.gridnine.spf.updater.handlers;

import java.io.File;

public class StopSpfAppHandler implements CommandHandler{

    private final File spfDir;

    public StopSpfAppHandler(File spfDir){
        this.spfDir = spfDir;
    }
    @Override
    public String getCommand() {
        return "STOP_APP";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {
        File file = new File(spfDir, "temp/.lock");
        if(!file.exists() || file.delete()){
            return "OK".getBytes();
        }
        File shellFile = new File(spfDir, "bin/stop.sh");
        if(!shellFile.exists()){
            throw new Exception("stop file does not exist " + shellFile.getAbsolutePath() );
        }
        //Process process = Runtime.getRuntime().exec("sh \"" + shellFile.getAbsolutePath() + "\"", new String[]{}, shellFile.getParentFile());
        Process process = Runtime.getRuntime().exec(shellFile.getAbsolutePath(), new String[]{}, shellFile.getParentFile());
        int exitValue = process.waitFor();
        if(exitValue != 0){
            throw new Exception("unable to stop process, exit code = " + exitValue);
        }
        return "OK".getBytes();
    }
}
