package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/gptresponses")
public class GPTcontroller {
    @Autowired
    private GPTservice gptService;
    @GetMapping //GET request
    public List<GPTresponse> getAllGPTresponses() {
        return gptService.getAllGPTresponses();
    }

    @PostMapping //POST request
    public GPTresponse createResponse(@RequestBody GPTresponse response) {
        return gptService.createResponse(response);
    }
    //HTTP requesting
}
@Service
@RequiredArgsConstructor //No args constructor (Lombok)
class GPTservice {
    private final GPTrepository gptRepository; //DB communication

    public List<GPTresponse> getAllGPTresponses() {
        return gptRepository.findAll();
    }
    public GPTresponse createResponse(GPTresponse response){
        return gptRepository.save(response);
    }
    //Business logic
}
@Repository
interface GPTrepository extends JpaRepository<GPTresponse, Long> {
    //DB connection
}