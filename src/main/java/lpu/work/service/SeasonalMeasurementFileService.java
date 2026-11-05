package lpu.work.service;

import lpu.work.entity.SeasonalMeasurementFile;
import lpu.work.entity.SeasonalMeasurementYear;
import lpu.work.repository.SeasonalMeasurementFileRepository;
import lpu.work.repository.SeasonalMeasurementYearRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.core.io.Resource;

@Service
public class SeasonalMeasurementFileService {

    private final SeasonalMeasurementFileRepository fileRepository;
    private final SeasonalMeasurementYearRepository yearRepository;

    private final Path uploadDirectory;

    public SeasonalMeasurementFileService(
            SeasonalMeasurementFileRepository fileRepository,
            SeasonalMeasurementYearRepository yearRepository,           
            @Value("${app.upload.measure-dir}") String uploadDirectory) {

        this.fileRepository = fileRepository;
        this.yearRepository = yearRepository;        
        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public SeasonalMeasurementFile uploadFile(
            Long yearId,
            String title,
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран.");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Укажи название файла.");
        }

        String originalFilename = Paths.get(
                file.getOriginalFilename() == null
                        ? "file"
                        : file.getOriginalFilename())
                .getFileName().toString();

        if (originalFilename.isBlank()) {
            throw new IllegalArgumentException("Некорректное имя файла.");
        }

        SeasonalMeasurementYear year = yearRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException("Указанный год не найден."));

        // Получаем расширение исходного файла
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        // Генерируем уникальное имя
        String storedFilename = UUID.randomUUID() + extension;

        Path target = uploadDirectory.resolve(storedFilename).normalize();

        if (!target.getParent().equals(uploadDirectory)) {
            throw new IllegalArgumentException("Некорректный путь к файлу.");
        }

        Files.createDirectories(uploadDirectory);

        // Сохраняем файл на диске
        file.transferTo(target);

        try {
            SeasonalMeasurementFile measurementFile = new SeasonalMeasurementFile();

            measurementFile.setMeasurementYear(year);
            measurementFile.setTitle(title.trim());
            measurementFile.setStoredFilename(storedFilename);
            measurementFile.setOriginalFilename(originalFilename);
            measurementFile.setUploadDate(LocalDateTime.now());

            return fileRepository.save(measurementFile);

        } catch (RuntimeException e) {
            // Если запись в БД не удалась — удаляем файл с диска
            Files.deleteIfExists(target);
            throw e;
        }
    }

    public Resource downloadFile(Long fileId) throws IOException {

        SeasonalMeasurementFile measurementFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Файл не найден в базе данных."));

        Path filePath = uploadDirectory
                .resolve(measurementFile.getStoredFilename())
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Некорректный путь к файлу.");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IOException("Файл отсутствует на диске.");
        }

        return new org.springframework.core.io.UrlResource(
                filePath.toUri());
    }
}
