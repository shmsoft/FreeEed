/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.ec2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.freeeed.util.OsUtil;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class S3Agent {

    private S3Service s3Service;
    private final int BUF_SIZE = 64 * 1024; // 64K, just a good-looking number, need to justify
    private static final Logger logger = LoggerFactory.getLogger(S3Agent.class);

    private void connect() throws S3ServiceException {
        Settings settings = Settings.getSettings();
        String awsAccessKey = settings.getAccessKeyId();
        String awsSecretKey = settings.getSecretAccessKey();
        AWSCredentials awsCredentials =
                new AWSCredentials(awsAccessKey, awsSecretKey);
        s3Service = new RestS3Service(awsCredentials);
    }

    public String[] getBucketList() throws S3ServiceException {
        connect();
        List<String> buckets = new ArrayList<String>();
        S3Bucket[] myBuckets = s3Service.listAllBuckets();
        for (S3Bucket bucket : myBuckets) {
            buckets.add(bucket.getName());
        }
        return buckets.toArray(new String[0]);
    }

    public boolean isConnectionGood() {
        try {
            connect();
            getBucketList();
            return true;
        } catch (S3ServiceException e) {
            return false;
        }
    }

    public String createBucket(String bucketName) {
        try {
            connect();
            S3Bucket bucket = s3Service.createBucket(bucketName);
            return bucket.getName();
        } catch (S3ServiceException e) {
            return null;
        }
    }

    public String[] getProjectList() {
        Settings settings = Settings.getSettings();
        String projectBucket = settings.getProjectBucket();
        List<String> projects = new ArrayList<>();
        try {
            connect();
            S3Object[] objects = s3Service.listObjects(projectBucket);
            for (S3Object object : objects) {
                String key = object.getKey();
                if (key.endsWith(".project")) {
                    projects.add(key);
                }
            }
        } catch (S3ServiceException e) {
            logger.error("Could not list project bucket '{}'", projectBucket);
        }
        return projects.toArray(new String[0]);
    }

    public boolean getProjectFromS3(String projectName) {
        Settings settings = Settings.getSettings();
        try {
            connect();
            S3Object objectComplete = s3Service.getObject(settings.getProjectBucket(), projectName);

            InputStream in = objectComplete.getDataInputStream();
            FileOutputStream out = new FileOutputStream(projectName);
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
            return true;
        } catch (Exception e) {
            logger.error("Could not get project", e);
            return false;
        }
    }

    public String getTextFromS3(String bucket, String fileKey) {
        try {
            connect();
            S3Object objectComplete = s3Service.getObject(bucket, fileKey);
            InputStream in = objectComplete.getDataInputStream();
            int max_size = 10000;
            char[] buf = new char[max_size];
            int i = 0;
            int c;
            while ((c = in.read()) != -1) {
                buf[i++] = (char) c;
            }
            char[] info = new char[i];
            System.arraycopy(buf, 0, info, 0, i);
            return new String(info);
        } catch (ServiceException | IOException e) {
            return null;
        }
    }

    public void downloadFileFromS3(String bucket, String fileKey, String outputFileName) {
        FileOutputStream fop = null;

        byte[] bytes = new byte[BUF_SIZE];
        try {
            connect();
            S3Object objectComplete = s3Service.getObject(bucket, fileKey);
            InputStream in = objectComplete.getDataInputStream();

            File file = new File(outputFileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fop = new FileOutputStream(file);
            int c;
            while ((c = in.read(bytes)) != -1) {
                fop.write(bytes, 0, c);
            }
        } catch (ServiceException | IOException e) {
            logger.error("Problem downloading", e);
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                logger.error("Problem downloading", e);
            }
        }
    }

    public boolean getStagedFileFromS3(String fileKey, String outputFile) {
        logger.info("Getting file from {} and putting it into {}", fileKey, outputFile);
        FileOutputStream out = null;
        byte[] bytes = new byte[BUF_SIZE];
        try {
            connect();
            String bucket = fileKey.substring("s3://".length(), fileKey.indexOf("/", "s3://".length()));
            logger.trace("bucket={}", bucket);
            String fileName = fileKey.substring(fileKey.indexOf("/", "s3://".length()) + 1);
            logger.trace("fileName={}", fileName);
            S3Object objectComplete = s3Service.getObject(bucket, fileName);
            InputStream in = objectComplete.getDataInputStream();
            out = new FileOutputStream(outputFile);
            int c;
            while ((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
            }
            return true;
        } catch (ServiceException | IOException e) {
            logger.error("Error gettings file: ", e);
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                logger.error("Error", e);
                return false;
            }
        }

    }

    public boolean getFilesFromS3(String filter, String outputDir) {
        logger.info("Getting files from {} and putting them into {}", filter, outputDir);
        FileOutputStream out = null;
        new File(outputDir).mkdirs();
        Settings settings = Settings.getSettings();
        byte[] bytes = new byte[BUF_SIZE];
        try {
            connect();
            String bucket = settings.getProjectBucket();
            String delimiter = null; // Refer to the S3 guide for more information on delimiters
            S3Object[] filteredObjects = s3Service.listObjects(bucket, filter, delimiter);
            for (S3Object object : filteredObjects) {
                S3Object objectComplete = s3Service.getObject(bucket, object.getKey());
                InputStream in = objectComplete.getDataInputStream();
                out = new FileOutputStream(outputDir + new File(object.getKey()).getName());
                int c;
                while ((c = in.read(bytes)) != -1) {
                    out.write(bytes, 0, c);
                }
                out.close();
            }
            return true;
        } catch (ServiceException | IOException e) {
            logger.error("Error", e);
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                logger.error("Error", e);
                return false;
            }
        }
    }

    public boolean putProjectInS3(Project project, String projectKey) {
        Settings settings = Settings.getSettings();
        try {
            connect();
            String stringData = project.toString();

            S3Object stringObject = new S3Object(projectKey, stringData);
            s3Service.putObject(settings.getProjectBucket(), stringObject);
            logger.info("Project uploaded successfull: {}", projectKey);
        } catch (S3ServiceException | NoSuchAlgorithmException | IOException e) {
            logger.error("Error putting project in S3", e);
            return false;
        }
        return true;
    }

    public boolean putFileInS3(String fileName, String s3key) {
        Settings settings = Settings.getSettings();
        String bucket = settings.getProjectBucket();
        logger.info("Putting file {} into bucket {} with key {}", fileName, bucket, s3key);
        try {
            connect();
            File fileData = new File(fileName);
            S3Object fileObject = new S3Object(fileData);
            fileObject.setKey(s3key);
            s3Service.putObject(bucket, fileObject);
            logger.info("Successfully copied file from {} to S3 bucket with key {}", 
                    fileName, settings.getProjectBucket(), s3key);
        } catch (S3ServiceException | NoSuchAlgorithmException | IOException e) {
            logger.error("Error putting file into bucket", e);
            return false;
        }
        return true;
    }

    public static String pathToKey(String fileName) {
        String s3key = fileName.substring(Project.getCurrentProject().getOut().length() + 1);
        if (OsUtil.isWindows()) {
            String backslash = "\\\\";
            String forwardslash = "/";
            s3key = s3key.replaceAll(backslash, forwardslash);
        }
        return s3key;
    }

    /**
     * This function assumes that connect() was called before We don't want to call connect() on every invocation
     *
     * @param s3key
     * @return
     * @throws java.lang.Exception
     */
    public long getFileSize(String s3key) throws Exception {
        connect();
        Settings settings = Settings.getSettings();
        String projectBucket = settings.getProjectBucket();
        String fileName = s3key.substring("s3://".length() + projectBucket.length() + 1);
        S3Object[] objectList =
                s3Service.listObjects(projectBucket, fileName, null);
        return objectList[0].getContentLength();
    }
}
