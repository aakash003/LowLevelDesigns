package org.flipkart;

import org.flipkart.entity.User;
import org.flipkart.entity.tasks.Bug;
import org.flipkart.entity.tasks.Feature;
import org.flipkart.entity.tasks.Story;
import org.flipkart.entity.tasks.SubTask;
import org.flipkart.enums.Status;
import org.flipkart.helper.ServiceFactory;
import org.flipkart.service.SprintManagerService;
import org.flipkart.service.TaskManagerService;
import org.flipkart.util.UniqueIdGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);


    public static void main(String[] args) {

            SprintManagerService sprintManagerService = ServiceFactory.getSprintManagerService();
            TaskManagerService taskManagerService = ServiceFactory.getTaskManagerService();



            User user1 = new User("Brad");
            User user2 = new User("Aditya");
            Feature feature1 = new Feature("Create Dashboard", "sdgfsdfsdf",user1, user2,  "5/04/2024", "LOW");
            Bug bug1 = new Bug("Error handling", user1, user2, "1/04/2023", "P0");
            Story story1 = new Story("Story1", user2, user1, "10/04/2025", "Story summary1");
            SubTask subTask1 = new SubTask("Subtask1", Status.COMPLETED);



            sprintManagerService.createNewSprint("R221");

            sprintManagerService.addTaskToSprint("R221", feature1);
            sprintManagerService.addTaskToSprint("R221", bug1);
            sprintManagerService.addTaskToSprint("R221", story1);

            taskManagerService.addSubTaskToTask(subTask1, "Story1");




            //sprintManagerService.displaySprintDelayedTask("R221");
            taskManagerService.changeStatusOfTask("Story1", Status.COMPLETED);

            taskManagerService.displayTaskAssignedToUser("Brad");
            taskManagerService.displayTaskAssignedToUser("Aditya");


//            sprintManagerService.displaySprintDelayedTask("R221");
//
//
//            taskManagerService.changeStatusOfTask("Create Dashboard", Status.IN_PROGRESS);
//
//            taskManagerService.displayTaskAssignedToUser("Brad");
//            sprintManagerService.deleteSprint("R224");

    }




}