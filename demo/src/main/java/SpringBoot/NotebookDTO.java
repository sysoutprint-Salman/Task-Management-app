package SpringBoot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotebookDTO {
    private Long id;
    private String tabTitle;
    private String notebookText;
}
