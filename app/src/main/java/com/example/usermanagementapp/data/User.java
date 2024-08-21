package com.example.usermanagementapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    public long id;
    public String email;
    public String first_name;
    public String last_name;
    //public String Name;
    public String job;
    public String avatar;

    public long getId() {
        return id;
    }

//    public void setName(String name) {
//        this.Name = name;
//    }

    public void setEmail(String name) {
        this.email = name + "@reqres.in";

    }

    public String getEmail() {
        return email;

    }

    public void setJob(String job) {
        this.job =  job;

    }
    public void setId(long id) {
        this.id  = id;

    }
    public String getName() {
        return first_name + " " +last_name;
    }
    public String getJob() {
        return job;
    }


//    public void setName(String name) {
//        String[] nameParts = name.split(" ");
//        this.firstName = nameParts[0];
//        if (nameParts.length > 1) {
//            this.lastName = nameParts[1];
//        } else {
//            this.lastName = "";
//        }
//    }
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }


    public String getAvatar() {
        return avatar;
    }
}