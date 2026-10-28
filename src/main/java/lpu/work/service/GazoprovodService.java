package lpu.work.service;

import lpu.work.entity.Gazoprovod;
import lpu.work.repository.GazoprovodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GazoprovodService {

    private final GazoprovodRepository gazoprovodRepository;

    public GazoprovodService(GazoprovodRepository gazoprovodRepository) {
        this.gazoprovodRepository = gazoprovodRepository;
    }

    public List<Gazoprovod> findAll() {
        return gazoprovodRepository.findAll();
    }
}
