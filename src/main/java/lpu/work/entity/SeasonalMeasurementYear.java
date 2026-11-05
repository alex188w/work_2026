package lpu.work.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seasonal_measurement_years")
public class SeasonalMeasurementYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false, unique = true)
    private Integer year;

    public SeasonalMeasurementYear() {
    }

    public SeasonalMeasurementYear(Integer year) {
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
