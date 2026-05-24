package com.rudra.sessionbased_task_tracker.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rudra.sessionbased_task_tracker.user.exception.ImageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final Cloudinary cloudinary;

    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) {

        if (file.isEmpty()) {
            throw new ImageUploadException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageUploadException("Only image files are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageUploadException("File size exceeds 5MB limit");
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", UUID.randomUUID().toString()
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new ImageUploadException("Cloudinary did not return a secure URL");
            }

            return (String) secureUrl;

        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image", e);
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (IOException e) {
            log.warn("Failed to delete image from Cloudinary: {}", imageUrl, e);
        }
    }

    private String extractPublicId(String imageUrl) {
        String withoutExtension = imageUrl.substring(0, imageUrl.lastIndexOf('.'));
        return withoutExtension.substring(withoutExtension.lastIndexOf('/') + 1);
    }
}