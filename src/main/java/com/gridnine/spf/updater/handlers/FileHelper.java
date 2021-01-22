package com.gridnine.spf.updater.handlers;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class FileHelper {
    static String calculateCheckSum(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        if(file.getName().endsWith(".zip") || file.getName().endsWith(".jar") || file.getName().endsWith(".war")){
            try(FileInputStream fis = new FileInputStream(file)){
                try(ZipInputStream zipStrm =new ZipInputStream(fis)){
                    updateZipDigest(md, zipStrm);
                }
            }
        } else {
            try(FileInputStream fis = new FileInputStream(file)){
                updateDigest(md, fis);
            }
        }
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    private static void updateZipDigest(final MessageDigest md,
                                        final ZipInputStream strm) throws IOException {
        ZipEntry entry;
        while ((entry = strm.getNextEntry()) != null) {
            String entryName = entry.getName();
            md.update(entryName.getBytes(StandardCharsets.UTF_8));
            if (entryName.endsWith("/")) {
                continue;
            }
            entryName = entryName.toLowerCase();
            if (entryName.endsWith(".zip") || entryName.endsWith(".jar") || entryName.endsWith(".war")) {
                updateZipDigest(md, new ZipInputStream(strm));
            } else {
                updateDigest(md, strm);
            }
        }
    }

    private static void updateDigest(final MessageDigest md,
                                     final InputStream strm) throws IOException {
        byte[] buf = new byte[256];
        int len;
        while ((len = strm.read(buf)) != -1) {
            md.update(buf, 0, len);
        }
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
        File[] files = spfLibDir.listFiles();
        if(files != null) {
            for (File file :files) {
                if (file.isFile()) {
                    result.put(file.getName(), FileHelper.calculateCheckSum(file));
                }
            }
        }
        return result;
    }
}
