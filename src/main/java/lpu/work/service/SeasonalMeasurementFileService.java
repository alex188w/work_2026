package lpu.work.service;

import lpu.work.entity.SeasonalMeasurementFile;
import lpu.work.entity.SeasonalMeasurementYear;
import lpu.work.repository.SeasonalMeasurementFileRepository;
import lpu.work.repository.SeasonalMeasurementYearRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import java.nio.file.Paths;

@Service
public class SeasonalMeasurementFileService {

    private final SeasonalMeasurementFileRepository fileRepository;
    private final SeasonalMeasurementYearRepository yearRepository;

    public SeasonalMeasurementFileService(
            SeasonalMeasurementFileRepository fileRepository,
            SeasonalMeasurementYearRepository yearRepository) {

        this.fileRepository = fileRepository;
        this.yearRepository = yearRepository;
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
            throw new IllegalArgumentException(
                    "Некорректное имя файла.");
        }

        SeasonalMeasurementYear year = yearRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Указанный год не найден."));

        SeasonalMeasurementFile measurementFile =
                new SeasonalMeasurementFile();

        measurementFile.setMeasurementYear(year);
        measurementFile.setTitle(title.trim());
        measurementFile.setOriginalFilename(originalFilename);
        measurementFile.setUploadDate(LocalDateTime.now());

        // Сохраняем содержимое файла в PostgreSQL
        measurementFile.setFileData(file.getBytes());

        return fileRepository.save(measurementFile);
    }

    public Resource downloadFile(
            SeasonalMeasurementFile measurementFile) {

        return new ByteArrayResource(
                measurementFile.getFileData());
    }
}
