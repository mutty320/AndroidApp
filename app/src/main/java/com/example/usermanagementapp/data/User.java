package com.example.usermanagementapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity
public class User {
    @PrimaryKey
    private long id;
    private String email;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    private String job;
    private String avatar;

    public String getName() {
        return firstName + " " + lastName;
    }
    public String getJob() {
        return this.job;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() { return email; }
    public String getAvatar() {
        return avatar;
    }
    public long getId() {
        return id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName)  {
        this.lastName = lastName;
    }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setEmail(String email) { this.email = email; }
    public void setJob(String job) { this.job =  job; }
    public void setId(long id) { this.id  = id;}

}