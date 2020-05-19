package com.gridnine.spf.updater.handlers;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class StartSpfAppHandler implements CommandHandler{

    private final File spfDir;

    private final int spfPort;

    private final int waitTimeInSeconds;

    public StartSpfAppHandler(File spfDir, int spfPort, int waitTimeInSeconds){
        this.spfDir = spfDir;
        this.spfPort = spfPort;
        this.waitTimeInSeconds = waitTimeInSeconds;
    }
    @Override
    public String getCommand() {
        return "START_APP";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {
        File file = new File(spfDir, "temp/lock.tmp");
        if(file.exists()){
            throw new Exception("Unable to delete lock file, app is already running");
        }
        File shellFile = new File(spfDir, "bin/run.sh");
        if(!shellFile.exists()){
            throw new Exception("stop file does not exist " + shellFile.getAbsolutePath() );
        }
        Process process = Runtime.getRuntime().exec(shellFile.getAbsolutePath(), new String[]{}, shellFile.getParentFile());
        int exitValue = process.waitFor();
        if(exitValue != 0){
            throw new Exception("unable to start process, exit code = " + exitValue);
        }
        long startTime = System.currentTimeMillis();
        while (true){
            if(isApplicationRunning()){
                break;
            }
            if(System.currentTimeMillis()-startTime > TimeUnit.SECONDS.toMillis(waitTimeInSeconds)){
                   throw new Exception("application does not start after 30 seconds");
            }
            Thread.sleep(1000L);
        }
        return "OK".getBytes();
    }

    private boolean isApplicationRunning(){
        try {
            try (Socket socket = new Socket(InetAddress.getByName("localhost"), spfPort)) {
                socket.setKeepAlive(true);
                InputStream in = null;
                final String test = "" + System.currentTimeMillis();
                try (OutputStream out = socket.getOutputStream()) {
                    out.write(("PING: "+ test).getBytes());
                    out.flush();
                    socket.shutdownOutput();
                    in = socket.getInputStream();
                    StringBuilder commandResult = new StringBuilder();
                    byte[] buf = new byte[16];

                    int len;
                    while ((len = in.read(buf)) != -1) {
                        commandResult.append(new String(buf, 0, len));
                    }

                    socket.shutdownInput();
                    String result = commandResult.toString();
                    return result.startsWith("OK") && result.contains(test);
                } finally {
                    if (in != null) in.close();
                }
            }
        } catch (Exception e){
            return false;
        }
    }
}
