package lpu.work.repository;

import lpu.work.entity.Work;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkRepository extends JpaRepository<Work, Long> {
    List<Work> findByUstanovkaId(Long ustanovkaId);    
    List<Work> findByUstanovkaIdOrderByWorkDateDesc(Long ustanovkaId);
    List<Work> findByWorkDateBetweenOrderByWorkDateAsc(LocalDate start, LocalDate end);
    List<Work> findByDoneFalseAndWorkDateBefore(LocalDate date);

    @Query("SELECT w FROM Work w LEFT JOIN FETCH w.images WHERE w.id = :id")
    Optional<Work> findByIdWithImages(@Param("id") Long id);
}
