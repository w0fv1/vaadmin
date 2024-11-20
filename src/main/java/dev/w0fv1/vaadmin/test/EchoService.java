package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.GenericRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class EchoService {
    private final GenericRepository genericRepository;

    public EchoService(GenericRepository genericRepository) {
        this.genericRepository = genericRepository;
    }

    @Transactional
    public String updateMessage(Long id, String input) {
        Echo echo = genericRepository.find(id, Echo.class);
        if (echo == null) {
            echo = new Echo();
        }
        echo.setMessage(input);
        return echo.getMessage();
    }
}
