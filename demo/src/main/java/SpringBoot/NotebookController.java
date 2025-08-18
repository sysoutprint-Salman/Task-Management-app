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
    @PostMapping
    public ResponseEntity<?> postNotebook(@RequestBody Notebook notebook){
        notebookService.postNotebook(notebook);
        return ResponseEntity.ok("Notebook successfully created.");
    }
    @PutMapping("/{id}/tab")
    public ResponseEntity<?> updateNotebookTab(@PathVariable Long id, @RequestBody Notebook notebook){
            Notebook existingNotebook = notebookService.getNotebook(id)
                    .orElseThrow(() -> new RuntimeException("Notebook not found."));
            existingNotebook.setTabTitle(notebook.getTabTitle());
            notebookService.postNotebook(existingNotebook);
            return ResponseEntity.ok("Tab updated successfully");
    }
    //PUT implementation for the notepad auto-saving
    @PutMapping("/{id}/text")
    public ResponseEntity<?> updateNotebookText(@PathVariable Long id, @RequestBody Notebook notebook){
        Notebook existingNotebook = notebookService.getNotebook(id)
                .orElseThrow(() -> new RuntimeException("Notebook not found."));
        existingNotebook.setNotebookText(notebook.getNotebookText());
        notebookService.postNotebook(existingNotebook);
        return ResponseEntity.ok("Notebook text updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotebook(@PathVariable Long id){
        notebookService.deleteNotebook(id);
        return ResponseEntity.ok("Notebook deleted successfully.");
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
}
@Repository
interface NotebookRepository extends JpaRepository<Notebook, Long> {

}
