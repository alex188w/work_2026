package lpu.work.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seasonal_measurement_files")
public class SeasonalMeasurementFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Год, к которому относится файл
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false)
    private SeasonalMeasurementYear measurementYear;

    // Название, которое увидит пользователь
    @Column(nullable = false)
    private String title;

    // Уникальное имя файла на диске
    @Column(name = "stored_filename", nullable = false, unique = true)
    private String storedFilename;

    // Исходное имя загруженного файла
    @Column(name = "original_filename")
    private String originalFilename;

    // Дата загрузки
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    public SeasonalMeasurementFile() {
    }

    public Long getId() {
        return id;
    }

    public SeasonalMeasurementYear getMeasurementYear() {
        return measurementYear;
    }

    public void setMeasurementYear(SeasonalMeasurementYear measurementYear) {
        this.measurementYear = measurementYear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}