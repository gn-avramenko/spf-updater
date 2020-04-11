package com.gridnine.spf.updater.handlers;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class FileHelper {
    static String calculateCheckSum(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Files.readAllBytes(Paths.get(file.toURI())));
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    static void syncDirs(File spfLibDir, File file) throws Exception {
        Map<String, String> spfFiles = getInfo(spfLibDir);
        Map<String, String> localFiles = getInfo(file);
        Set<String> toDelete = new HashSet<>(localFiles.keySet());
        for(Map.Entry<String, String> localFile: localFiles.entrySet()){
            String spfFile = spfFiles.get(localFile.getKey());
            if(localFile.getValue().equals(spfFile)){
                toDelete.remove(localFile.getKey());
                spfFiles.remove(localFile.getKey());
            }
        }
        for(String fileName : toDelete){
            if(!new File(file, fileName).delete()) throw new Exception("unable to delete file " + fileName);
        }
        for(String fileName : spfFiles.keySet()){
            Files.copy(Paths.get(new File(spfLibDir, fileName).toURI()), Paths.get(new File(file, fileName).toURI()));
        }
    }

    static Map<String, String> getInfo(File spfLibDir) throws Exception {
        Map<String, String> result = new HashMap<>();
        for (File file : spfLibDir.listFiles()) {
            if(file.isFile()){
                result.put(file.getName(), FileHelper.calculateCheckSum(file));
            }
        }
        return result;
    }
}
