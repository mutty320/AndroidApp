package com.example.usermanagementapp.ui.main;

import com.example.usermanagementapp.data.User;
import java.util.List;

public interface MainContract {

    interface View {
        void showUsers(List<User> users);
        void showUserAdded(User user);
        void showUserUpdated(User user);
        void showUserDeleted(User user);
        void showError(String message);
        void showClearAllUsers();

    }

    interface Presenter {
        void clearAllUsers();
        void loadUsers(int  page);
        void addUser(User user);
        void updateUser(User user);
        void deleteUser(User user);
    }
}
