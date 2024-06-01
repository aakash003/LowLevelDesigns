package org.flipkart.service;

import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.Sprint;
import org.flipkart.entity.tasks.Task;
import org.flipkart.helper.ServiceFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
public class SprintManagerService {

    private static final Logger log = LogManager.getLogger(SprintManagerService.class);
    List<Sprint> sprintList = new ArrayList<>();

    public void createNewSprint(String sprintName){
        sprintList.add(new Sprint(sprintName));
}

    public Sprint getSprint(String sprintName){
        for(Sprint sprint: sprintList){
            if(sprint.getSprintName().equals(sprintName)){
                return sprint;
            }
        }
        log.error("No sprint found with name: " + sprintName + ", returning NULL");
        return null;
    }

    public void addTaskToSprint(String sprintName, Task task){
        TaskManagerService taskManagerService = ServiceFactory.getTaskManagerService();
        taskManagerService.addTaskList(task);
        Sprint sprint = getSprint(sprintName);
        if(sprint != null) {
            List<Task> taskList = sprint.getTaskArrayList();
            taskList.add(task);
            log.info("task added to sprint successfully");
        }else{
            log.error("task failed as no sprint found with that name");
        }
    }

    public void deleteSprint(String sprintName){
        Sprint sprint = getSprint(sprintName);
        if(sprint == null){
            log.info("Sprint does not exist, task completed");
        }else{
            sprintList.remove(sprint);
            log.info("Sprint: " + sprintName + " removed successfully");
        }
    }

    public void displaySprintDelayedTask(String sprintName){
        Sprint sprint = getSprint(sprintName);
        if(sprint != null){
            List<Task> taskList = sprint.getTaskArrayList();
            for(Task task: taskList){
                log.info("Sprint Title: " + task.getTitle() + " Task Status: " + task.getStatus());
                LocalDate currentDate = LocalDate.now();
                if(task.getDueDate().isBefore(currentDate)){
                    log.info("CURRENT TASK STATUS: " + "DELAYED");
                }else{
                    log.info("CURRENT TASK STATUS: " + "ON TRACK");
                }
            }
        }
    }

}
