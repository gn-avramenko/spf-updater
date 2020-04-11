package com.gridnine.spf.updater.boot;

import com.gridnine.spf.updater.handlers.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SpfUpdater {
    public static void main(String[] args) throws Exception {
        Properties properties = initProperties();
        String mode = System.getProperty("mode");

        int port = Integer.parseInt(properties.getProperty("port", "21567"));
        if ("stop".equals(mode)) {
            new StopSpfUpdateHandler(port).handle();
            return;
        }
        String spfDir = properties.getProperty("spfDir", System.getProperty("spfDir"));
        if(spfDir == null){
            throw new Exception("spfDir is not defined neither in config no in system property");
        }
        File spfDirFile = new File(spfDir);
        if(!spfDirFile.exists()) throw new Exception("spfDir does not exists "+spfDirFile.getAbsolutePath());
        File startScript = new File(spfDirFile, "bin/"+properties.getProperty("startScript", System.getProperty("startScript", "run.sh")));
        if(!startScript.exists())  throw new Exception("startScript does not exists "+startScript.getAbsolutePath());
        File stopScript = new File(spfDirFile, "bin/"+properties.getProperty("stopScript", System.getProperty("stopScript","stop.sh")));
        if(!stopScript.exists())  throw new Exception("stopScript does not exists "+stopScript.getAbsolutePath());
        int spfPort = Integer.parseInt(properties.getProperty("spfPort", System.getProperty("spfPort", "21566")));
        String tempDirectory = properties.getProperty("tempDirectory", System.getProperty("tempDirectory", "temp"));
        FileLockHandler fileLockHadler = new FileLockHandler(tempDirectory);
        if ("start".equals(mode)) {
            System.out.println("Press 'q' key to exit.");
            int c;
            do {
                try {
                    c = System.in.read();
                } catch (IOException var2) {
                    break;
                }
            } while ('q' != (char) c && 'Q' != (char) c);
            fileLockHadler.releaseLock();
            return;
        }

        ControlThread controlThread = new ControlThread(port, fileLockHadler::releaseLock);
        controlThread.register(new StopCommandHandler(fileLockHadler::releaseLock));
        File spfLibDir = new File(spfDirFile, "lib");
        controlThread.register(new GetExistingFilesInfoHandler(spfLibDir));
        controlThread.register(new InitLocalRepositoryHandler(spfLibDir));
        controlThread.register(new DeleteFileHandler());
        controlThread.register(new AddFileHandler());
        controlThread.register(new StopSpfAppHandler(spfDirFile));
        controlThread.register(new UpdateLibHandler(spfLibDir));
        controlThread.register(new StartSpfAppHandler(spfDirFile, spfPort, 30));
        controlThread.start();
        System.out.println("application started in BACKGROUND mode");
    }

    private static Properties initProperties() throws IOException {
        File configFile = new File(System.getProperty("config", "config/boot.properties"));
        Properties properties = new Properties();
        if (configFile.exists()) {
            try (InputStream is = new FileInputStream(configFile)) {
                properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
        return properties;
    }

}
