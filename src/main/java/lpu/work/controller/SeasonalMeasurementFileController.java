package lpu.work.controller;

import lpu.work.entity.SeasonalMeasurementFile;
import lpu.work.repository.SeasonalMeasurementFileRepository;
import lpu.work.service.SeasonalMeasurementFileService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/seasonal-measurements")
public class SeasonalMeasurementFileController {

    private final SeasonalMeasurementFileService fileService;
    private final SeasonalMeasurementFileRepository fileRepository;

    public SeasonalMeasurementFileController(
            SeasonalMeasurementFileService fileService,
            SeasonalMeasurementFileRepository fileRepository) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam Long yearId,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            fileService.uploadFile(yearId, title, file);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Файл успешно загружен.");

        } catch (IOException | IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Ошибка загрузки файла: " + e.getMessage());
        }

        return "redirect:/seasonal-measurements";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {

        try

        {
            SeasonalMeasurementFile measurementFile = fileRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Файл не найден."));

            Resource resource = fileService.downloadFile(id);

            String filename = measurementFile.getTitle();

            String contentDisposition = org.springframework.http.ContentDisposition
                    .attachment()
                    .filename(filename, StandardCharsets.UTF_8)
                    .build()
                    .toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
