package org.flipkart.entity.tasks;


import lombok.Getter;
import lombok.Setter;
import org.flipkart.entity.User;
import org.flipkart.enums.Status;
import org.flipkart.enums.TaskType;

import java.time.LocalDate;

@Getter
@Setter
public abstract class Task {

    private String title;
    private User user;
    private User assignedBy;
    private Status status;
    private TaskType type;
    private LocalDate dueDate;


}
