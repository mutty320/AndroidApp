package com.example.usermanagementapp.network;

public class UserResponse {

    private String name;
    private String job;
    private String id;
    private String createdAt;

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
