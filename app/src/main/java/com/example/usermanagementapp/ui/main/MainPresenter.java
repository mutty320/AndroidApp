package com.example.usermanagementapp.ui.main;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

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
    private final MainContract.View view;
    private final AppDatabase db;
    private final ReqResApi api;
    private final SharedPreferences sharedPreferences;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private static final int PAGE_SIZE = 7;   // Number of users to load per page
    private static final int TOTAL_API_PAGES = 2;
    public MainPresenter(MainContract.View view, AppDatabase db, SharedPreferences sharedPreferences) {
        this.view = view;
        this.db = db;
        this.sharedPreferences = sharedPreferences;
        this.api = ApiClient.getRetrofitInstance().create(ReqResApi.class);
    }

    @Override
    public void loadUsers(int page) {
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            loadUsersFromApi(1);
        } else {
            loadUsersFromDatabase(page);
        }
    }

    private void loadUsersFromApi(int page) {
        api.getUsers(page).enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> usersFromApi = response.body().getData();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        for (User user : usersFromApi) {
                            User existingUser = db.userDao().getUserById((int) user.getId());
                            if (existingUser == null) {
                                user.setAvatar(UserUtils.generateAvatarUrl(user.getId()));
                                db.userDao().insert(user);
                            }
                        }
                        // If there are more pages, load them sequentially
                        if (page < TOTAL_API_PAGES) {
                            loadUsersFromApi(page + 1);
                        } else {
                            // Once all pages are loaded, load from the database and show in UI
                            loadUsersFromDatabase(1);
                            sharedPreferences.edit().putBoolean("isFirstRun", false).apply();
                        }
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

    private void loadUsersFromDatabase(int page) {
        int offset = (page - 1) * MainPresenter.PAGE_SIZE;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<User> users = db.userDao().getUsersByPage(offset, MainPresenter.PAGE_SIZE);
            runOnMainThread(() -> {
                if (users.isEmpty()) {
                    view.showError("No more users to load");
                } else {
                    view.showUsers(users);
                }
            });
        });
    }

    @Override
    public void updateUser(User user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserRequest userRequest = new UserRequest(user.getFirstName(), user.getJob());
            api.updateUser(user.getId(), userRequest).enqueue(new Callback<UpdateResponse>() {
                @Override
                public void onResponse(@NonNull Call<UpdateResponse> call, @NonNull Response<UpdateResponse> response) {
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
                public void onFailure(@NonNull Call<UpdateResponse> call, @NonNull Throwable t) {
                    runOnMainThread(() -> view.showError("API call failed: " + t.getMessage()));
                }
            });
        });
    }
    private boolean validateUser(User user) {
        User existingUserByName = db.userDao().getUserByFullName(user.getFirstName(), user.getLastName());
        User existingUserByEmail = db.userDao().getUserByEmail(user.getEmail());

        if (existingUserByName != null) {
            runOnMainThread(() -> view.showError("User with this name already exists."));
            return false;
        }

        if (existingUserByEmail != null) {
            runOnMainThread(() -> view.showError("User with this email already exists."));
            return false;
        }

        return true;
    }

    @Override
    public void addUser(User user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Validate the user before proceeding
            if (!validateUser(user)) {
                return; // Stop further execution if validation fails
            }

            UserRequest userRequest = new UserRequest(user.getFirstName() + " " + user.getLastName(), user.getJob());
            api.createUser(userRequest).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        long newUserId = response.body().getId();

                        Executors.newSingleThreadExecutor().execute(() -> {
                            if (db.userDao().getUserById((int) newUserId) == null) {
                                user.setId(newUserId);
                                user.setAvatar(UserUtils.generateAvatarUrl(user.getId()));
                                db.userDao().insert(user);
                                runOnMainThread(() -> {
                                    view.showUserAdded(user);
                                    view.showError("User added successfully");
                                });
                            } else {
                                runOnMainThread(() -> view.showError("User ID already exists."));
                            }
                        });
                    } else {
                        runOnMainThread(() -> view.showError("Failed to create user"));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                    runOnMainThread(() -> view.showError("API call failed: " + t.getMessage()));
                }
            });
        });
    }

    @Override
    public void deleteUser(User user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            long userId = user.getId();

            api.deleteUser(userId).enqueue(new Callback<Void>() {

                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.userDao().delete(user);
                            runOnMainThread(() -> view.showUserDeleted(user));
                        });
                    } else {
                        runOnMainThread(() -> view.showError("Failed to delete user"));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    runOnMainThread(() -> view.showError("API call failed: " + t.getMessage()));
                }
            });
        });
    }

    @Override
    public void clearAllUsers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.userDao().clearAll();
            runOnMainThread(view::showClearAllUsers);
        });
    }
    private void runOnMainThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }
}
