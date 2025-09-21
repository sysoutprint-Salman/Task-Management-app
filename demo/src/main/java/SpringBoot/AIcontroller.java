package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/gptresponses")
@RequiredArgsConstructor
public class AIcontroller {
    private final AIservice gptService;
    @GetMapping
    public List<AI> getAllGPTresponses() {
        return gptService.getAllGPTresponses();
    }

    @PostMapping
    public AI createResponse(@RequestBody AI response) {
        return gptService.createResponse(response);
    }
    @GetMapping("filter")
    public List<AI> findByUserId(@RequestParam Long userId){
        return gptService.findByUserId(userId);
    }
}
@Service
@RequiredArgsConstructor
class AIservice {
    private final AIrepository gptRepository;

    public List<AI> getAllGPTresponses() {
        return gptRepository.findAll();
    }
    public AI createResponse(AI response){
        return gptRepository.save(response);
    }
    public List<AI> findByUserId(Long userId){
        return gptRepository.findByUserId(userId);
    }
}
@Repository
interface AIrepository extends JpaRepository<AI, Long> {
    List<AI> findByUserId(Long userId);
}