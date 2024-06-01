package org.flipkart.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.User;
import org.flipkart.entity.tasks.Story;
import org.flipkart.entity.tasks.SubTask;
import org.flipkart.entity.tasks.Task;
import org.flipkart.enums.Status;
import org.flipkart.enums.TaskType;

import java.util.ArrayList;
import java.util.List;

public class TaskManagerService {

    private static final Logger log = LogManager.getLogger(TaskManagerService.class);

    private List<Task> taskList = new ArrayList<>();


    public void addTaskList(Task task){
        taskList.add(task);
    }

    public Task getTaskFromTaskList(String taskTitle){
        for(Task task: taskList){
            if(task.getTitle().equals(taskTitle))
                return task;
        }

        log.error("No task found !!");
        return null;
    }

    public boolean addSubTaskToTask(SubTask subTask, String taskTitle){
        Task task = getTaskFromTaskList(taskTitle);
        if(task != null) {
            if (task.getType().equals(TaskType.STORY) && task.getStatus() != Status.COMPLETED) {
                Story story = (Story) task;
                List<SubTask> subTaskList = story.getSubTaskArrayList();
                subTaskList.add(subTask);
                return true;
            } else {
                log.error("taskTitle not of correct type or correct status, operation failed");

            }
        }
        return false;
    }

    public void changeStatusOfTask(String taskTitle, Status status){
        Task task = getTaskFromTaskList(taskTitle);
        if(task!= null) {
            if (task.getType().equals(TaskType.STORY)) {
                if (validateStoryStatusChange((Story) task)) {
                    task.setStatus(status);
                }
            } else {
                task.setStatus(status);
            }
        }
    }

    public void changeAssigneeOfTask(User user, String taskTitle){
        Task task = getTaskFromTaskList(taskTitle);
        if(task != null){
            task.setUser(user);
        }
    }
    private boolean validateStoryStatusChange(Story task){
        List<SubTask> subTaskList = task.getSubTaskArrayList();

        for(SubTask subTask: subTaskList){
            if(subTask.getStatus() != Status.COMPLETED){
                return false;
            }
        }
        log.info("All the subtask for the given story are completed: "+ task.getTitle());
        return true;
    }

    public void displayTaskAssignedToUser(String userName){
        log.info("Printing task assigned to user: " + userName);
        for(Task task: taskList){
            if(task.getUser().getName().equals(userName)){
                log.info("TASK: " + task.getTitle() + " STATUS: " + task.getStatus());
            }


        }

    }

}
