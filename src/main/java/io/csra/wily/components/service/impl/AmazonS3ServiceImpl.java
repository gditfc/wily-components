package io.csra.wily.components.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.csra.wily.components.service.AmazonS3Service;
import org.apache.commons.io.IOUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AmazonS3ServiceImpl implements AmazonS3Service {

    private final Environment environment;

    private final AmazonS3 s3Client;

    private static final String AWS_S3_BUCKET_NAME_KEY = "aws.s3.bucket.name";

    public AmazonS3ServiceImpl(Environment environment, AmazonS3 s3Client) {
        this.environment = environment;
        this.s3Client = s3Client;
    }

    @Override
    public byte[] getDocumentFromS3(String documentKey) throws IOException {
        try (S3Object fullObject = s3Client.getObject(new GetObjectRequest(environment.getRequiredProperty(AWS_S3_BUCKET_NAME_KEY), documentKey))) {
            // Get an object and print its contents.
            return IOUtils.toByteArray(fullObject.getObjectContent());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process it, so it returned an error response.
            throw new IOException(e);
        }
        // To ensure that the network connection doesn't remain open, close any open input streams.
    }

    @Override
    public void uploadDocumentToS3(MultipartFile file, String documentKey, boolean isPublic) throws IOException {
        File localFile = null;
        try {
            // Upload a file as a new object with ContentType and title specified.
            localFile = toFile(file);
            PutObjectRequest request = new PutObjectRequest(environment.getRequiredProperty(AWS_S3_BUCKET_NAME_KEY), documentKey, localFile);
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
        s3Client.deleteObject(new DeleteObjectRequest(environment.getRequiredProperty(AWS_S3_BUCKET_NAME_KEY), documentKey));
    }


    private File toFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile != null && multipartFile.getOriginalFilename() != null) {
            File convFile = new File(multipartFile.getOriginalFilename());
            convFile.createNewFile();

            try (final FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(multipartFile.getBytes());
            }

            return convFile;
        }

        throw new IOException("Uploaded File Does Not Exist!");
    }
}
