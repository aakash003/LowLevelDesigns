package org.flipkart.entity.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.flipkart.enums.Status;


@Setter
@Getter
@AllArgsConstructor
public class SubTask {

    private String title;

    private Status status;
}
