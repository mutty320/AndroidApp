package com.example.usermanagementapp.network;

public class UserResponse {

    private String name;
    private String job;
    private int id;
    private String createdAt;


//    public UserResponse(String name, String job, int id) {
//        this.name = name;
//        this.job = job;
//        this.id = id;
//    }

    public String getName() {
        return name;
    }
    public String getJob() {
        return job;
    }
    public int getId() {
        return id;
    }

}
