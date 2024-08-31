package ru.itis.kpfu.selyantsev.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "t_task")
public class Task {

    @Id
    @UuidGenerator
    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "task_name", length = 20)
    private String taskName;

    @Column(name = "is_complete")
    private boolean isComplete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
}
