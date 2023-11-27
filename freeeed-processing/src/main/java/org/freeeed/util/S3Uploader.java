package org.freeeed.util;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.core.sync.RequestBody;

import java.nio.file.Paths;

public class S3Uploader {
    public void uploadFile(String bucketName, String keyName, String filePath) {
        S3Client s3 = S3Client.builder()
                .region(Region.US_EAST_2) // specify your region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .acl(ObjectCannedACL.PUBLIC_READ) // making the file public
                    .build();

            s3.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(filePath)));
            System.out.println("File uploaded successfully and is now public.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while uploading the file.");
        } finally {
            s3.close();
        }
    }
}
