package lpu.work.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "work_image")
public class WorkImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @ManyToOne
    @JoinColumn(name = "work_id")
    private Work work;
}
