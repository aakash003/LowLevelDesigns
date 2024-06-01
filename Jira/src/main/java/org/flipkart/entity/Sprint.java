package org.flipkart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flipkart.entity.tasks.Task;

import java.util.ArrayList;



@Getter
@Setter
public class Sprint {

    private String sprintName;
    ArrayList<Task> taskArrayList;

    public Sprint(String sprintName) {
        this.sprintName = sprintName;
        this.taskArrayList = new ArrayList<>();
    }
}
