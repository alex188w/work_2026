package lpu.work.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "ustanovkas")
public class Ustanovka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type; // катодная / дренажная / и т.д.

    @ManyToOne
    @JoinColumn(name = "gazoprovod_id")
    private Gazoprovod gazoprovod;

    @OneToMany(mappedBy = "ustanovka")
    private List<Work> works;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Gazoprovod getGazoprovod() {
        return gazoprovod;
    }

    public List<Work> getWorks() {
        return works;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGazoprovod(Gazoprovod gazoprovod) {
        this.gazoprovod = gazoprovod;
    }

    public void setWorks(List<Work> works) {
        this.works = works;
    }
}