package lpu.work.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lpu.work.entity.WorkImage;

import java.util.List;

@Repository
public interface WorkImageRepository extends JpaRepository<WorkImage, Long> {

    // 🔥 полезный метод — получить все картинки для работы
    List<WorkImage> findByWorkId(Long workId);
}
