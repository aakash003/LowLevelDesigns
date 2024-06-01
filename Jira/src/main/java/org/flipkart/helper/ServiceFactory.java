package org.flipkart.helper;

import org.flipkart.service.SprintManagerService;
import org.flipkart.service.TaskManagerService;

public class ServiceFactory {

    private static TaskManagerService taskManager;
    private static SprintManagerService sprintManagerService;

    public static TaskManagerService getTaskManagerService(){
        if(taskManager == null){
            taskManager = new TaskManagerService();
        }

        return taskManager;
    }

    public static SprintManagerService getSprintManagerService(){
        if(sprintManagerService == null){
            sprintManagerService = new SprintManagerService();
        }

        return sprintManagerService;
    }
}
