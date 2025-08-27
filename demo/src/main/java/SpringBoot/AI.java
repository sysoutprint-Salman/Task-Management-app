package SpringBoot;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gpt_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String prompt;
    @Column(columnDefinition = "TEXT")
    private String response;
    private LocalDateTime timestamp;
}
