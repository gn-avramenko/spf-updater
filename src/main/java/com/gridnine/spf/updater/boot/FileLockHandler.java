package com.gridnine.spf.updater.boot;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

class FileLockHandler {

    private final FileLock fileLock;

    FileLockHandler(String tempDirectory) throws Exception {
        File tempDir = new File(tempDirectory);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new Exception("unable to create dir " + tempDir);
        }
        File file = new File(tempDir, ".lock");
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            file.deleteOnExit();
            fileLock = new RandomAccessFile(file, "rwd").getChannel().tryLock();
        } catch (Exception e) {
            throw new Exception(
                    "Another instance of the application is running. Please terminate and try again.", e);
        }
        if(fileLock == null){
            throw new Exception(
                    "Another instance of the application is running. Please terminate and try again.");
        }
    }

    void releaseLock(){
        if (fileLock != null && fileLock.acquiredBy().isOpen()) {
            try {
                fileLock.release();
                fileLock.channel().close();
            } catch (IOException e) {
                System.out.println("error releasing lock");
                e.printStackTrace();
            }
        }
    }

}
