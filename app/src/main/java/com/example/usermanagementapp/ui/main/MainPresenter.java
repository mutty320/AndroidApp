package com.example.usermanagementapp.ui.main;

import android.os.Handler;
import android.os.Looper;

import com.example.usermanagementapp.data.AppDatabase;
import com.example.usermanagementapp.data.User;
import com.example.usermanagementapp.network.ApiClient;
import com.example.usermanagementapp.network.ReqResApi;
import com.example.usermanagementapp.network.UserRequest;
import com.example.usermanagementapp.network.UserResponse;
import com.example.usermanagementapp.network.UsersResponse;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter implements MainContract.Presenter {

    private MainContract.View view;
    private AppDatabase db;
    private ReqResApi api;
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public MainPresenter(MainContract.View view, AppDatabase db) {
        this.view = view;
        this.db = db;
        this.api = ApiClient.getRetrofitInstance().create(ReqResApi.class);
    }

    @Override
    public void loadUsers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<User> users = db.userDao().getAllUsers();
            // Update UI on main thread
            runOnMainThread(() -> view.showUsers(users));
        });

        api.getUsers(2).enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> usersFromApi = response.body().getData();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        for (User user : usersFromApi) {
                            User existingUser = db.userDao().getUserById((int)user.getId());
                            if (existingUser == null) {
                                db.userDao().insert(user);
                            }
                        }
                        runOnMainThread(() -> view.showUsers(db.userDao().getAllUsers()));
                    });
                } else {
                    view.showError("Failed to load users from API");
                }
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                view.showError("API call failed: " + t.getMessage());
            }
        });
    }


    @Override
    public void addUser(String name, String job) {
        UserRequest newUserRequest = new UserRequest(name, job);
        api.createUser(newUserRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User newUser = new User();
                    newUser.setId(response.body().getId());
                    newUser.setName(response.body().getName());
                    newUser.setJob(response.body().getJob());
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.userDao().insert(newUser);
                        // Update UI on main thread
                        runOnMainThread(() -> view.showUserAdded(newUser));
                    });
                } else {
                    view.showError("Failed to add user");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                view.showError("API call failed: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteUser(User user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.userDao().delete(user);
            // Update UI on main thread
            runOnMainThread(() -> view.showUserDeleted(user));
        });
    }

    // Utility method to run code on the main thread
    private void runOnMainThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }
}
