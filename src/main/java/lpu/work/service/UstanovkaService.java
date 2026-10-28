package lpu.work.service;

import lpu.work.entity.Ustanovka;
import lpu.work.repository.UstanovkaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UstanovkaService {

    private final UstanovkaRepository repository;

    public UstanovkaService(UstanovkaRepository repository) {
        this.repository = repository;
    }

    public List<Ustanovka> findByType(String type) {
        return repository.findByTypeOrderByIdAsc(type);
    }
}
