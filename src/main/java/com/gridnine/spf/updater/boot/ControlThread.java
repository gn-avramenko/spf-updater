/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF-UPDATER
 *****************************************************************/

package com.gridnine.spf.updater.boot;

import com.gridnine.spf.updater.handlers.CommandHandler;
import com.gridnine.spf.updater.handlers.StopAppCallback;
import com.gridnine.spf.updater.handlers.StopCommandHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
final class ControlThread extends Thread {
    private final ServerSocket serverSocket;
    private final StopAppCallback stopAppCallback;
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();


    ControlThread(int port,  StopAppCallback callback) throws Exception {

        this.serverSocket = new ServerSocket(port, 1, null);
        this.stopAppCallback = callback;
        this.setName("spf-updater-control-thread");
    }

    void register(CommandHandler handler){
        commandHandlers.put(handler.getCommand(), handler);
    }
    public void run() {
        try {
            while (true) {
                try {
                    try(Socket clientSocket = this.serverSocket.accept()){
                        if (this.handleRequest(clientSocket)) {
                            break;
                        }
                    } catch (Exception e){
                        //noops
                    }
                } catch (Exception e) {
                    println("error on server socket");
                    e.printStackTrace();
                    break;
                }
            }
        } finally {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                println("error closing server socket");
                e.printStackTrace();
            }
            this.stopAppCallback.stopApplication();
        }

    }

    private synchronized boolean handleRequest(Socket clientSocket) {

        boolean result = false;


        try (InputStream in =clientSocket.getInputStream()){
            clientSocket.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            clientSocket.shutdownInput();
            byte[] bytes = baos.toByteArray();
            byte commandLength = bytes[0];
            byte[] command = new byte[commandLength];
            if (commandLength >= 0) System.arraycopy(bytes, 2, command, 0, commandLength);
            String commandStr  = new String(command, StandardCharsets.UTF_8);
            byte paramsLength = bytes[1];
            byte[] params = new byte[paramsLength];
            if(paramsLength > 0) {
                int offset = 2 + commandLength;
                int maxCount = 2 + commandLength + paramsLength;
                if (maxCount > commandLength)
                    System.arraycopy(bytes, offset, params, 0, paramsLength);
            }
            String paramsString = new String(params, StandardCharsets.UTF_8);
            CommandHandler handler = commandHandlers.get(commandStr);
            byte[] commandResult;
            if(handler == null){
                commandResult = ("ERROR: no handler found for command " + commandStr).getBytes();
            } else {
                try {
                    int offset = 2 + commandLength + paramsLength;
                    byte[] body = new byte[bytes.length - offset];
                    if (bytes.length - offset >= 0)
                        System.arraycopy(bytes, offset, body, 0, bytes.length - offset);
                    commandResult = handler.handleCommand(paramsString, body);
                    result = handler.isStopCommand();
                } catch (Exception e){
                    e.printStackTrace();
                    commandResult = ("ERROR: " + e.getMessage()).getBytes();
                }
            }
            try (OutputStream out = clientSocket.getOutputStream()){
                out.write(commandResult);
                out.flush();
                clientSocket.shutdownOutput();
            }
        } catch (Exception e) {
            println("error processing control request");
            e.printStackTrace();
        }
        return result;
    }




    private static void println(String text) {
        System.out.println(text);
    }



}
