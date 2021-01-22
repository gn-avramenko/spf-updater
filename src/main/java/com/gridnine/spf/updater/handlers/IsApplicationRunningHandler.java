package com.gridnine.spf.updater.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class IsApplicationRunningHandler implements CommandHandler{

    private int appPort;

    public IsApplicationRunningHandler(int appPort){
        this.appPort = appPort;
    }
    @Override
    public String getCommand() {
        return "IS_APP_RUNNING";
    }

    @Override
    public byte[] handleCommand(String parameters, byte[] body) throws Exception {

        try {
            try (Socket socket = new Socket("localhost", appPort)) {
                socket.setKeepAlive(true);
                InputStream in = null;
                try (OutputStream out = socket.getOutputStream()) {
                    final String test = "" + System.currentTimeMillis();
                    out.write(("PING " + test).getBytes());
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
                    if(result.startsWith("OK") && result.contains(test)){
                        return "OK".getBytes();
                    }
                    return "ERROR".getBytes();
                } finally {
                    if (in != null) in.close();
                }
            }
        } catch (IOException e) {
            return "ERROR".getBytes();
        }
    }
}
