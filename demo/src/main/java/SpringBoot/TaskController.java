package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService; //Serivce class instantiation
    private final DeletedTaskService deletedTaskService;

    @GetMapping //GET request
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    @PostMapping //POST request
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found with id: " + id);
        }
    }

    /*@DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok("Task deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found.");
        }
    }*/
}
