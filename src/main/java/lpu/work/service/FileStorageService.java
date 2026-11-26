package lpu.work.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;

// @Service
// public class FileStorageService {

//     private final String uploadDir = System.getProperty("user.dir") + "/uploads/works/";

//     public String saveFile(MultipartFile file, Long ustanovkaId) throws IOException {

//         Path uploadPath = Paths.get(uploadDir, String.valueOf(ustanovkaId));
//         Files.createDirectories(uploadPath);

//         String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

//         Path filePath = uploadPath.resolve(fileName);

//         Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

//         System.out.println("SAVED TO: " + filePath);

//         return fileName;
//     }
// }

@Service
public class FileStorageService {

    private final Path uploadDirectory;

    public FileStorageService(
            @Value("${app.upload.works-dir}") String uploadDirectory) {

        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public String saveFile(MultipartFile file, Long ustanovkaId)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран.");
        }

        Path uploadPath = uploadDirectory
                .resolve(String.valueOf(ustanovkaId))
                .normalize();

        if (!uploadPath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Некорректный путь.");
        }

        Files.createDirectories(uploadPath);

        String originalFilename = Paths.get(
                file.getOriginalFilename() == null
                        ? "file"
                        : file.getOriginalFilename())
                .getFileName().toString();

        String fileName = UUID.randomUUID()
                + "_"
                + originalFilename;

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}