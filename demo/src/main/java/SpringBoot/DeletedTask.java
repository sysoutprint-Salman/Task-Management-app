package SpringBoot;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deletedTasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletedTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;
    private String title;
    private LocalDate date;
    private String description;
    private LocalDateTime deletedDate;

}
