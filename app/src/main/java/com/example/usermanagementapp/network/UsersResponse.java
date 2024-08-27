package com.example.usermanagementapp.network;

import com.example.usermanagementapp.data.User;
import java.util.List;

public class UsersResponse {
    private int page;
    private int per_page;
    private int total;
    private int total_pages;
    private List<User> data;

    public List<User> getData() {
        return data;
    }

}
