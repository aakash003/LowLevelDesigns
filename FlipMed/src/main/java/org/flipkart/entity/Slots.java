package org.flipkart.entity;

public class Slots {
    private String startTime;
    private String slotId;
    private String endTime;

    @Override
    public String toString() {
        return "Slots{" +
                "startTime='" + startTime + '\'' +
                ", slotId='" + slotId + '\'' +
                ", endTime='" + endTime + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }

    private boolean isAvailable;


    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

}
