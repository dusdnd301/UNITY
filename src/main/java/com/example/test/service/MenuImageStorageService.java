package com.example.test.service;

import com.example.test.config.AppProperties;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MenuImageStorageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final AppProperties properties;

    public MenuImageStorageService(AppProperties properties) {
        this.properties = properties;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 선택해주세요.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("이미지는 5MB 이하만 업로드할 수 있습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        String extension = extension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        try {
            Path dir = Path.of(properties.menuImageOutputDir());
            Files.createDirectories(dir);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            return "/menu-images/" + fileName;
        } catch (Exception ex) {
            throw new IllegalStateException("메뉴 이미지 저장에 실패했습니다.");
        }
    }

    private String extension(String originalFilename) {
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일 이름이 올바르지 않습니다.");
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("이미지 확장자가 필요합니다.");
        }
        String extension = originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("jpg, png, webp, gif 이미지만 업로드할 수 있습니다.");
        }
        return extension;
    }
}
