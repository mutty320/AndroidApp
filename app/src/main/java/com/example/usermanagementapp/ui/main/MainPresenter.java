package com.example.usermanagementapp.ui.main;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.usermanagementapp.data.AppDatabase;
import com.example.usermanagementapp.data.User;
import com.example.usermanagementapp.data.UserUtils;
import com.example.usermanagementapp.network.ApiClient;
import com.example.usermanagementapp.network.ReqResApi;
import com.example.usermanagementapp.network.UserRequest;
import com.example.usermanagementapp.network.UserResponse;
import com.example.usermanagementapp.network.UsersResponse;
import com.example.usermanagementapp.network.UpdateResponse;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter implements MainContract.Presenter {
    private static final String TAG = "MainActivity";
    private final MainContract.View view;
    private final AppDatabase db;
    private final ReqResApi api;
    private final SharedPreferences sharedPreferences;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private int currentPage = 1; // To track the current page
    private int pageSize = 4; // To define the number of users per page

    public MainPresenter(MainContract.View view, AppDatabase db, SharedPreferences sharedPreferences) {
        this.view = view;
        this.db = db;
        this.sharedPreferences = sharedPreferences;
        this.api = ApiClient.getRetrofitInstance().create(ReqResApi.class);
    }


    @Override
    public void loadUsers(int page) {

        // Load users from the database in a background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            List<User> users = db.userDao().getAllUsers();
            runOnMainThread(() -> view.showUsers(users));
        });

        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            loadUsersFromApi();
        }
    }
    private void loadUsersFromApi() {
        api.getUsers(currentPage).enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> usersFromApi = response.body().getData();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        for (User user : usersFromApi) {
                            User existingUser = db.userDao().getUserById((int) user.getId());
                            if (existingUser == null) {
                                db.userDao().insert(user);
                            }
                        }
                        List<User> updatedUsers = db.userDao().getAllUsers();
                        runOnMainThread(() -> view.showUsers(updatedUsers));

                        // Mark initial load as complete
                        sharedPreferences.edit().putBoolean("isFirstRun", false).apply();
                    });
                } else {
                    runOnMainThread(() -> view.showError("Failed to load users from API"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {
                runOnMainThread(() -> view.showError("API call failed: " + t.getMessage()));
            }
        });
    }



    @Override
    public void updateUser(User user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserRequest userRequest = new UserRequest(user.getFirstName(), user.getJob());
            api.updateUser(user.getId(), userRequest).enqueue(new Callback<UpdateResponse>() {
                @Override
                public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UpdateResponse updatedUser = response.body();
                        user.setFirstName(updatedUser.getName());
                        user.setJob(updatedUser.getJob());

                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.userDao().update(user);
                            runOnMainThread(() -> view.showUserUpdated(user));
                        });
                    } else {
                        runOnMainThread(() -> view.showError("Failed to update user"));
                    }
                }

                @Override
                public void onFailure(Call<UpdateResponse> call, Throwable t) {
                    runOnMainThread(() -> view.showError("API call failed: " + t.getMessage()));
                }
            });
        });
    }

    @Override
    public void addUser(User user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserRequest userRequest = new UserRequest(user.getFirstName() + " " + user.getLastName(), user.getJob());
            api.createUser(userRequest).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        long newUserId = response.body().getId();

                        // Move the database check to a background thread
                        Executors.newSingleThreadExecutor().execute(() -> {
                            if (db.userDao().getUserById((int) newUserId) == null) {
                                user.setId(newUserId);
                                user.setAvatar(UserUtils.generateAvatarUrl(user.getId()));
                                db.userDao().insert(user);
                                runOnMainThread(() -> view.showUserAdded(user));
                            } else {
                                runOnMainThread(() -> view.showError("User ID already exists."));
                            }
                        });
                    } else {
                        runOnMainThread(() -> view.showError("Failed to create user"));
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    runOnMainThread(() -> view.showError("API call failed: " + t.getMessage()));
                }
            });
        });
    }

    @Override
    public void deleteUser(User user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            long userId = user.getId();

            // Now, make the API call to delete the user by ID
            api.deleteUser(userId).enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // If successful, delete the user locally
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.userDao().delete(user);
                            runOnMainThread(() -> view.showUserDeleted(user));
                        });
                    } else {
                        runOnMainThread(() -> view.showError("Failed to delete user"));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    runOnMainThread(() -> view.showError("API call failed: " + t.getMessage()));
                }
            });
        });
    }


    private void runOnMainThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }
}
