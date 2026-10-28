package lpu.work.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "gazoprovods")
public class Gazoprovod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "gazoprovod", fetch = FetchType.LAZY)   
    @OrderBy("type ASC, id ASC")
    private List<Ustanovka> ustanovkas;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Ustanovka> getUstanovkas() {
        return ustanovkas;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUstanovkas(List<Ustanovka> ustanovkas) {
        this.ustanovkas = ustanovkas;
    }
}
