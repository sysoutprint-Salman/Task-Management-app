package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService; //Serivce class instantiation
    private final DeletedTaskService deletedTaskService;
    private final TaskRepository taskRepository;

    @GetMapping //GET request
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    @PostMapping //POST request
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task TaskInfo){
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()){
            Task existingTask = task.get();
            existingTask.setTitle(TaskInfo.getTitle());
            existingTask.setDate(TaskInfo.getDate());
            existingTask.setDescription(TaskInfo.getDescription());
            taskRepository.save(existingTask);
            return ResponseEntity.ok("Task successfully updated.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}") //DELETE request
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try{
            Task task = taskService.getTaskById(id);
            DeletedTask deletedTask = new DeletedTask();
            deletedTask.setTaskId(task.getId());
            deletedTask.setTitle(task.getTitle());
            deletedTask.setDescription(task.getDescription());
            deletedTask.setDate(task.getDate());
            deletedTask.setDeletedDate(LocalDateTime.now());
            deletedTaskService.insertDeletedTask(deletedTask);
            taskService.deleteTask(id);
            return ResponseEntity.ok("Task deleted successfully.");
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task couldn't be deleted with id: " + id);
        }
    }
}
@Service
@RequiredArgsConstructor //constructor (Lombok)
class TaskService {

    private final TaskRepository taskRepository;
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    public Task getTaskById(Long id){
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
@Repository //Repo works as a data access layer which interacts with the database
interface TaskRepository extends JpaRepository<Task, Long> { //Repo uses Task type & Long for ID

/*
    List<Task> findByCompleted(boolean completed);
*/
}