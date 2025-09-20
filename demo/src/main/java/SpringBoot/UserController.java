package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public void saveUser(@RequestBody User user){
        userService.saveUser(user);
    }
    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }
    @GetMapping("/{id}")
    public User getUserById(Long id){
        return userService.getUserById(id);
    }
    @GetMapping("/existing")
    public boolean isUserExisting(@RequestParam String username, @RequestParam String email) {
        return userService.isUserExisting(username,email);
    }

    @GetMapping("/login")
    public ResponseEntity<User> getUserByUsernameOrEmail(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        return userService.findByUsernameOrEmail(username, email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
@Service
@RequiredArgsConstructor
class UserService{
    private final UserRepo userRepo;

    public void saveUser(User user){
        userRepo.save(user);
    }
    public List<User> getAllUsers(){
        return userRepo.findAll();
    }
    public User getUserById(Long id){
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("SpringBoot: User not found."));
    }
    public boolean isUserExisting(String username, String email){
        return userRepo.findByUsernameOrEmail(username, email).isPresent();
    }
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return userRepo.findByUsernameOrEmail(username, email);
    }
}
@Repository
interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);
}