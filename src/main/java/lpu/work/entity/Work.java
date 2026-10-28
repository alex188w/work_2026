package lpu.work.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
@Entity
@Table(name = "works")
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String typeOfWork;
    private LocalDate workDate;
    private Boolean done = false;
    private String auto;
    // private String description;

    private Double i;
    private Double u;
    private Double usum;
    private Double upol;
    private Double snv;
    private Double ee;

    private String workPerformers;
    private String workDescription;
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "ustanovka_id")
    private Ustanovka ustanovka;

    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<WorkImage> images = new ArrayList<>();

    // getters/setters - не нужны - lombok
}
