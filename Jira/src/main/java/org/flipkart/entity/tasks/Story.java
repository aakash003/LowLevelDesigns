package org.flipkart.entity.tasks;

import lombok.Getter;
import lombok.Setter;
import org.flipkart.entity.User;
import org.flipkart.enums.Status;
import org.flipkart.enums.TaskType;
import org.flipkart.util.DateConverter;

import java.util.ArrayList;


@Getter
@Setter
public class Story extends Task{

    private String storySummary;
    private ArrayList<SubTask> subTaskArrayList;

    private Status status;

    public Story(String title, User user, User assignedBy, String date, String storySummary) {
        this.setTitle(title);
        this.setUser(user);
        this.setAssignedBy(assignedBy);
        this.setDueDate(DateConverter.getDateFromString(date));
        this.storySummary = storySummary;
        this.setType(TaskType.STORY);
        this.subTaskArrayList = new ArrayList<>();
        this.status = Status.OPEN;
    }


}
