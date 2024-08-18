package com.example.usermanagementapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    public int id;
    public String email;
    public String firstName;
    public String lastName;
    public String job;
    public String avatar;

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.firstName = name;
    }

    public void setEmail(String name) {
        this.email = name + "@reqres.in";

    }

    public void setJob(String job) {
        this.job =  job;

    }
    public void setId(int id) {
        this.id  = id;

    }
}