package com.example.usermanagementapp.network;

public class UserResponse {

    private String name;
    private String job;
    private String id;
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
    public long getId() {
        return Long.parseLong(id);
    }

}
