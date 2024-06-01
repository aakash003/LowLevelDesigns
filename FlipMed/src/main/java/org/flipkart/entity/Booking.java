package org.flipkart.entity;

public class Booking {
    private String bookingId;

    public Booking(String bookingId, String patientName, String doctorName, String startTime, String endTime) {
        this.bookingId = bookingId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private String patientName;
    private String doctorName;
    private String startTime;
    private String endTime;

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
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
