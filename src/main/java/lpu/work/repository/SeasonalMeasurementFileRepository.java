package lpu.work.repository;

import lpu.work.entity.SeasonalMeasurementFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonalMeasurementFileRepository
        extends JpaRepository<SeasonalMeasurementFile, Long> {

    List<SeasonalMeasurementFile>
        findByMeasurementYearIdOrderByUploadDateDesc(Long yearId);
}
