package org.flipkart.entity.tasks;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.flipkart.entity.User;
import org.flipkart.enums.FeatureImpactType;
import org.flipkart.enums.Status;
import org.flipkart.enums.TaskType;
import org.flipkart.util.DateConverter;


@ToString
@Getter
@Setter
public class Feature extends Task{

    private FeatureImpactType impactType;
    private String featureSummary;

    public Feature(String title,String summary, User user, User assignedBy, String date, String impactType) {
        this.setFeatureSummary(summary);
        this.setTitle(title);
        this.setUser(user);
        this.setAssignedBy(assignedBy);
        this.setDueDate(DateConverter.getDateFromString(date));
        this.setStatus(Status.OPEN);
        this.setType(TaskType.FEATURE);
        this.impactType = FeatureImpactType.valueOf(impactType);
    }
}
