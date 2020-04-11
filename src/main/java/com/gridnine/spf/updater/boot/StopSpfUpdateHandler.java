package com.gridnine.spf.updater.boot;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

class StopSpfUpdateHandler {
    private final int port;
    StopSpfUpdateHandler(int port){
        this.port = port;
    }

    void handle() throws Exception {
        InetAddress host = InetAddress.getByName("localhost");
        Socket socket;
        try {
            socket = new Socket(host, port);
        } catch (Exception e){
            System.out.println("seems there is no control service on port " + port);
            return;
        }
        try{
            socket.setKeepAlive(true);
            InputStream in = null;
            try (OutputStream out = socket.getOutputStream()) {
                System.out.println("found running control service on " + host + ":" + port);
                byte[] commmand = "40STOP".getBytes();
                commmand[0] = 4;
                commmand[1] = 0;
                out.write(commmand);
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
                System.out.println(result.startsWith("OK")? "Stop command succeded": ("Unable to stop application:" + result));
            } finally {
                if (in != null) in.close();
            }
        }finally {
            socket.close();
        }
    }
}
