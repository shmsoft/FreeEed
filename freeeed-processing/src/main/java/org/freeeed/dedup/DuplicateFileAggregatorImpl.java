package org.freeeed.dedup;

import org.apache.log4j.Logger;
import org.freeeed.mail.EmlParser;
import org.freeeed.services.Util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class groups duplicate files
 * Created by nehaojha on 09/02/18.
 */
public class DuplicateFileAggregatorImpl implements DuplicateFileAggregator {

    private static final Logger LOGGER = Logger.getLogger(DuplicateFileAggregatorImpl.class);
    private final Map<String, List<String>> duplicatesBucket = new ConcurrentHashMap<>();

    @Override
    public Map<String, List<String>> groupDuplicateFiles(String directoryPath) throws Exception {
        groupDuplicates(new File(directoryPath));
        return duplicatesBucket;
    }

    private void groupDuplicates(File root) throws Exception {
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String checksum = getCheckSum(file);
                List<String> duplicateFiles = duplicatesBucket.get(checksum);
                if (Objects.nonNull(duplicateFiles)) {
                    duplicateFiles.add(file.getPath());
                } else {
                    duplicateFiles = new ArrayList<>();
                    duplicateFiles.add(file.getPath());
                    duplicatesBucket.put(checksum, duplicateFiles);
                }
            } else {
                groupDuplicates(file);
            }
        }
    }

    private String getCheckSum(File file) throws Exception {
        String extension = Util.getExtension(file.getName());
        if (extension.isEmpty() || "eml".equalsIgnoreCase(extension)) {
            LOGGER.debug("processing email " + file);
            return md5EmailHash(file);
        } else {
            return md5FileHash(file);
        }
    }

    private String md5EmailHash(File file) throws Exception {
        EmlParser emlParser = new EmlParser(file);
        StringBuilder data = new StringBuilder();
        List<String> to = emlParser.getTo();
        List<String> from = emlParser.getFrom();
        List<String> cc = emlParser.getCC();
        List<String> bcc = emlParser.getBCC();
        String subject = emlParser.getSubject();
        String content = emlParser.getContent();

        data.append(to).append(from).append(cc).append(bcc).append(subject).append(content);
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] hash = messageDigest.digest(data.toString().getBytes());
        return new BigInteger(1, hash).toString(16);
    }

    private String md5FileHash(File file) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        FileInputStream fileInputStream = new FileInputStream(file.getPath());
        byte[] buffer = new byte[8192];
        int numOfBytesRead;
        while ((numOfBytesRead = fileInputStream.read(buffer)) != -1) {
            messageDigest.update(buffer, 0, numOfBytesRead);
        }
        byte[] hash = messageDigest.digest();
        return new BigInteger(1, hash).toString(16);
    }
}
