package org.freeeed.dedup;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class groups duplicate files
 * Created by nehaojha on 09/02/18.
 */
public class DuplicateFileAggregatorImpl implements DuplicateFileAggregator {

    private static final Logger LOGGER = Logger.getLogger(DuplicateFileAggregatorImpl.class);
    private final Map<String, List<String>> duplicatesBucket = new ConcurrentHashMap<>();
    private final Map<Long, List<File>> potentialDuplicates = new HashMap<>();

    @Override
    public Map<String, List<String>> groupDuplicateFiles(String directoryPath) throws Exception {
        groupDuplicates(directoryPath);
        return duplicatesBucket.entrySet()
                .stream().filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void groupDuplicates(String directoryPath) throws Exception {
        groupPotentialDuplicates(directoryPath);
        potentialDuplicates.entrySet()
                .parallelStream()
                .filter(e -> e.getValue().size() > 1)
                .forEach(e -> e.getValue().forEach(file -> {
                    try {
                        String checksum = getCheckSum(file);
                        List<String> duplicateFiles = duplicatesBucket.get(checksum);

                        if (duplicateFiles != null) {
                            duplicateFiles.add(file.getPath());
                        } else {
                            duplicateFiles = new ArrayList<>();
                            duplicateFiles.add(file.getPath());
                            duplicatesBucket.put(checksum, duplicateFiles);
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Error finding checksum for file " + file.getName(), ex);
                    }
                }));
    }

    private void groupPotentialDuplicates(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                long fileSize = file.length();
                List<File> fileList = potentialDuplicates.get(fileSize);
                if (Objects.isNull(fileList)) {
                    fileList = new ArrayList<>();
                    fileList.add(file);
                    potentialDuplicates.put(fileSize, fileList);
                } else {
                    fileList.add(file);
                }
            } else if (file.isDirectory()) {
                groupPotentialDuplicates(file.getAbsolutePath());
            }
        }
    }

    private String getCheckSum(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
        byte[] buffer = new byte[8192];
        int numOfBytesRead;
        while ((numOfBytesRead = fileInputStream.read(buffer)) != -1) {
            messageDigest.update(buffer, 0, numOfBytesRead);
        }
        byte[] hash = messageDigest.digest();
        return new BigInteger(1, hash).toString(16);
    }

}
