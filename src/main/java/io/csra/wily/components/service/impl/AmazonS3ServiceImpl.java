package io.csra.wily.components.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.google.common.io.ByteStreams;
import io.csra.wily.components.service.AmazonS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class AmazonS3ServiceImpl implements AmazonS3Service {

    @Autowired
    private Environment environment;

    @Autowired
    private AmazonS3 s3Client;

    @Override
    public byte[] getDocumentFromS3(String documentKey) throws IOException {
        S3Object fullObject = null;
        try {
            // Get an object and print its contents.
            fullObject = s3Client.getObject(new GetObjectRequest(environment.getRequiredProperty("aws.s3.bucket.name"), documentKey));
            return ByteStreams.toByteArray(fullObject.getObjectContent());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process it, so it returned an error response.
            throw new IOException(e);
        } finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
            if (fullObject != null) {
                fullObject.close();
            }
        }
    }

    @Override
    public void uploadDocumentToS3(MultipartFile file, String documentKey, boolean isPublic) throws IOException {
        File localFile = null;
        try {
            // Upload a file as a new object with ContentType and title specified.
            localFile = toFile(file);
            PutObjectRequest request = new PutObjectRequest(environment.getRequiredProperty("aws.s3.bucket.name"), documentKey, localFile);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            request.setMetadata(metadata);

            if (isPublic) {
                request.withCannedAcl(CannedAccessControlList.PublicRead);
            }

            s3Client.putObject(request);
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process it, so it returned an error response.
            throw new IOException(e);
        } finally {
            if (localFile != null) {
                localFile.delete();
            }
        }
    }

    @Override
    public void deleteDocumentFromS3(String documentKey) {
        s3Client.deleteObject(new DeleteObjectRequest(environment.getRequiredProperty("aws.s3.bucket.name"), documentKey));
    }


    private File toFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile != null && multipartFile.getOriginalFilename() != null) {
            File convFile = new File(multipartFile.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(multipartFile.getBytes());
            fos.close();

            return convFile;
        }

        throw new IOException("Uploaded File Does Not Exist!");
    }
}
