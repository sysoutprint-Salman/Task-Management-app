package SpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor //No args constructor (Lombok)
public class TaskService {


    private final TaskRepository taskRepository;
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
   /* public List<Task> findByCompleted(boolean completed){
        return taskRepository.findByCompleted(completed);
    }*/


}
