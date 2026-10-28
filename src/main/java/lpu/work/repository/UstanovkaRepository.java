package lpu.work.repository;

import lpu.work.entity.Ustanovka;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UstanovkaRepository extends JpaRepository<Ustanovka, Long> {

    // List<Ustanovka> findByGazoprovodId(Long gazoprovodId);
    // List<Ustanovka> findByType(String type);

    // List<Ustanovka> findByGazoprovodIdOrderByIdAsc(Long gazoprovodId);

    List<Ustanovka> findByTypeOrderByIdAsc(String type);
}
