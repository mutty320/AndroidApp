package com.example.usermanagementapp.ui.main;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.usermanagementapp.data.AppDatabase;
import com.example.usermanagementapp.data.User;
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
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public MainPresenter(MainContract.View view, AppDatabase db) {
        this.view = view;
        this.db = db;
        this.api = ApiClient.getRetrofitInstance().create(ReqResApi.class);
    }

    @Override
    public void loadUsers() {
        // Load users from the database in a background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            List<User> users = db.userDao().getAllUsers();
            // Update the UI on the main thread
            runOnMainThread(() -> view.showUsers(users));
        });

        // Load users from the API
        api.getUsers(2).enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> usersFromApi = response.body().getData();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        for (User user : usersFromApi) {

                            Log.d(TAG, user.last_name);

                            User existingUser = db.userDao().getUserById((int) user.getId());
                            if (existingUser == null) {
                                db.userDao().insert(user);
                            }
                        }
                        // Load all users from the database and update the UI
                        List<User> updatedUsers = db.userDao().getAllUsers();
                        Log.d(TAG, "Updated user list size: " + updatedUsers.size());

                        runOnMainThread(() -> view.showUsers(updatedUsers));
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
