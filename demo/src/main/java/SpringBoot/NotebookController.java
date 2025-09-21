package SpringBoot;

import jakarta.websocket.server.PathParam;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notebooks")
@RequiredArgsConstructor
public class NotebookController {
    private final NotebookService notebookService;
    @GetMapping
    public List<Notebook> getAllNotebooks(){
        return notebookService.getAllNotebooks();
         /*notebooks.stream()
                .map(notebook -> new NotebookDTO(notebook.getId(), notebook.getTabTitle(), notebook.getNotebookText()))
                .collect(Collectors.toList());*/
    }
    @GetMapping("/{id}")
    public Notebook getNotebook(Long id){
        Optional<Notebook> notebook = notebookService.getNotebook(id);
        if (notebook.isPresent()){return notebook.get();}
        else {System.out.println("SpringBoot: Notebook not found. Returning empty Notebook.");}
        return new Notebook();
    }
    @GetMapping("/filter")
    public List<Notebook> findByUserId(@RequestParam Long userId){
        return notebookService.findByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<?> postNotebook(@RequestBody Notebook notebook){
        notebookService.postNotebook(notebook);
        return ResponseEntity.ok("SpringBoot: Notebook successfully created.");
    }
    @PutMapping("/{id}/tab")
    public ResponseEntity<?> updateNotebookTab(@PathVariable Long id, @RequestBody Notebook notebook){
            Notebook existingNotebook = notebookService.getNotebook(id)
                    .orElseThrow(() -> new RuntimeException("SpringBoot: Notebook not found."));
            existingNotebook.setTabTitle(notebook.getTabTitle());
            notebookService.postNotebook(existingNotebook);
            return ResponseEntity.ok("SpringBoot: Tab updated successfully");
    }
    //PUT implementation for the notepad auto-saving
    @PutMapping("/{id}/text")
    public ResponseEntity<?> updateNotebookText(@PathVariable Long id, @RequestBody Notebook notebook){
        Notebook existingNotebook = notebookService.getNotebook(id)
                .orElseThrow(() -> new RuntimeException("SpringBoot: Notebook not found."));
        existingNotebook.setNotebookText(notebook.getNotebookText());
        notebookService.postNotebook(existingNotebook);
        return ResponseEntity.ok("SpringBoot: Notebook text updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotebook(@PathVariable Long id){
        notebookService.deleteNotebook(id);
        return ResponseEntity.ok("SpringBoot: Notebook deleted successfully.");
    }
}
@Service
@RequiredArgsConstructor
class NotebookService{
private final NotebookRepository notebookRepository;

    public List<Notebook> getAllNotebooks() {
        return notebookRepository.findAll();
    }
    public Optional<Notebook> getNotebook(Long id){
        return notebookRepository.findById(id);
    }
    public Notebook postNotebook(Notebook notebook){
        return notebookRepository.save(notebook);
    }
    public void deleteNotebook(Long id){
        notebookRepository.deleteById(id);
    }
    public List<Notebook> findByUserId(Long userId){
        return notebookRepository.findByUserId(userId);
    }
}
@Repository
interface NotebookRepository extends JpaRepository<Notebook, Long> {
    List<Notebook> findByUserId(Long userId);
}
