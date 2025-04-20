package SpringBoot;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deleted-tasks")
@RequiredArgsConstructor
public class DeletedTaskController {
    private final DeletedTaskService deletedTaskService;
    private final TaskService taskService;
    @GetMapping
    public List<DeletedTask> getDeletedTasks(){
        return deletedTaskService.getDeletedTasks();
    }
    @PostMapping
    public DeletedTask insertDeletedTask (@RequestBody DeletedTask deletedTask){
        return deletedTaskService.insertDeletedTask(deletedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> recoveredTask(@PathVariable Long id){
        try {
            DeletedTask deletedTask = deletedTaskService.getDeletedTaskById(id);
            Task task = new Task();
            task.setTitle(deletedTask.getTitle());
            task.setDescription(deletedTask.getDescription());
            task.setDate(deletedTask.getDate());
            taskService.createTask(task);
            deletedTaskService.deleteDeletedClass(id);
            return ResponseEntity.ok("Task successfully recovered.");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deleted Task wasn't recovered");
        }

    }


}
@Service
@RequiredArgsConstructor
class DeletedTaskService{
    @Autowired
    private final DeletedTaskRepository deletedTaskRepository;
    public List<DeletedTask> getDeletedTasks(){
        return deletedTaskRepository.findAll();
    }
    public DeletedTask getDeletedTaskById(Long id){
        return deletedTaskRepository.getDeletedTaskById(id);
    }
    public void deleteDeletedClass(Long id){
        deletedTaskRepository.deleteById(id);
    }
    public DeletedTask insertDeletedTask(DeletedTask deletedTask){
        return deletedTaskRepository.save(deletedTask);
    }

}
@Repository
interface DeletedTaskRepository extends JpaRepository<DeletedTask,Long> {

     DeletedTask getDeletedTaskById(Long id);
}
