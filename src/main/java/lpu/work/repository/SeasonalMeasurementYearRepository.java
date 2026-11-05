package lpu.work.repository;

import lpu.work.entity.SeasonalMeasurementYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonalMeasurementYearRepository
        extends JpaRepository<SeasonalMeasurementYear, Long> {

    List<SeasonalMeasurementYear> findAllByOrderByYearDesc();

    Optional<SeasonalMeasurementYear> findByYear(Integer year);

    boolean existsByYear(Integer year);
}
