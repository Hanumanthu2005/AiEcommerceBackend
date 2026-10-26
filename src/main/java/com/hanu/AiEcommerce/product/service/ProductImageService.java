
package com.hanu.AiEcommerce.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadDirectory;

    public ProductImageService(
            @Value("${app.upload.dir}") String uploadDirectory) {
        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public String storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("An image file is required.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Image size must not exceed 5 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Only JPEG, PNG, and WebP images are allowed.");
        }

        String originalFilename =
                StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? ""
                                : file.getOriginalFilename());

        if (originalFilename.isBlank()
                || originalFilename.contains("..")
                || originalFilename.contains("/")
                || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        String extension = switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Unsupported image format.");
        };

        Files.createDirectories(uploadDirectory);

        String filename = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(filename).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid image path.");
        }

        file.transferTo(destination);

        return "/uploads/products/" + filename;
    }
}
