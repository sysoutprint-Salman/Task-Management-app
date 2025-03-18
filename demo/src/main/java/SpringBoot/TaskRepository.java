package SpringBoot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository //Repo works as a data access layer which interacts with the database
public interface TaskRepository extends JpaRepository<Task, Long> { //Repo uses Task type & Long for ID
/*
    List<Task> findByCompleted(boolean completed);
*/
}
