package org.tensorflow.yolo.model;

public class UserInfoDTO {
    private String name;
    private String email;
    private String major;
    private String job;
    private String history;

    public UserInfoDTO(String name, String email, String major, String job, String history) {
        this.name = name;
        this.email = email;
        this.major = major;
        this.job = job;
        this.history = history;
    }

    public UserInfoDTO() {
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
