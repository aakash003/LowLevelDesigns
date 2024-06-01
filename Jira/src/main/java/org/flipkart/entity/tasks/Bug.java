package org.flipkart.entity.tasks;

import org.flipkart.entity.User;
import org.flipkart.enums.BugSeverityType;
import org.flipkart.enums.Status;
import org.flipkart.enums.TaskType;
import org.flipkart.util.DateConverter;


public class Bug extends Task{

    private BugSeverityType severityType;


    public Bug(String title, User user, User assignedBy, String date, String severityType) {
        this.setTitle(title);
        this.setUser(user);
        this.setAssignedBy(assignedBy);
        this.setDueDate(DateConverter.getDateFromString(date));
        this.setStatus(Status.OPEN);
        this.setType(TaskType.BUG);
        this.severityType = BugSeverityType.valueOf(severityType);
    }
}
