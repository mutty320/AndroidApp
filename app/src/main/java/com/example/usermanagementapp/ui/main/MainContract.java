package com.example.usermanagementapp.ui.main;

import com.example.usermanagementapp.data.User;
import java.util.List;

public interface MainContract {

    interface View {
        void showUsers(List<User> users);
        void showUserAdded(User user);
        void showUserDeleted(User user);
        void showError(String message);
    }

    interface Presenter {
        void loadUsers();
        void addUser(String name, String job);
        void updateUser(String name);
        void deleteUser(String name);
    }
}
