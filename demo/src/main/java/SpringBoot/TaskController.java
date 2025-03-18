package SpringBoot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService; //Serivce class instantiation

    @GetMapping //GET request
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping //POST request
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    @DeleteMapping("/{id}") //DELETE request
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
