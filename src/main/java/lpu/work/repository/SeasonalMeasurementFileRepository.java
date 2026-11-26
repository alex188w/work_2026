package lpu.work.repository;

import lpu.work.entity.SeasonalMeasurementFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeasonalMeasurementFileRepository
        extends JpaRepository<SeasonalMeasurementFile, Long> {

    interface FileInfo {

        Long getId();

        String getTitle();

        String getOriginalFilename();

        LocalDateTime getUploadDate();
    }

    List<FileInfo>
        findByMeasurementYearIdOrderByUploadDateDesc(Long yearId);
}
