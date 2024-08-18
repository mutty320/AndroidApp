package com.example.usermanagementapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usermanagementapp.data.AppDatabase;
import com.example.usermanagementapp.data.User;
import com.example.usermanagementapp.data.UserAdapter;
import com.example.usermanagementapp.network.ApiClient;
import com.example.usermanagementapp.network.ReqResApi;
import com.example.usermanagementapp.network.UsersResponse;
import com.example.usermanagementapp.network.UserResponse;
import com.example.usermanagementapp.network.UserRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText nameEditText, jobEditText;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private AppDatabase db;
    private ReqResApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        jobEditText = findViewById(R.id.jobEditText);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Room database
        db = AppDatabase.getDatabase(this);

        // Initialize Retrofit API
        api = ApiClient.getRetrofitInstance().create(ReqResApi.class);

        // Load users from API and set up the RecyclerView adapter
        loadUsersFromApi();

        // Set up button click listeners
        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });

        Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //updateUser();
            }
        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteUser();
            }
        });
    }

    private void loadUsersFromApi() {
        Call<UsersResponse> call = api.getUsers(2);
        call.enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                if (response.isSuccessful()) {
                    UsersResponse usersResponse = response.body();
                    if (usersResponse != null) {
                        userList = usersResponse.getData();
                        userAdapter = new UserAdapter(userList);
                        recyclerView.setAdapter(userAdapter);
                        saveUsersToDatabase(userList);
                    }
                } else {
                    Log.d("MainActivity", "Request failed");
                }
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                Log.e("MainActivity", "API call failed", t);
            }
        });
    }

    private void saveUsersToDatabase(List<User> users) {
        for (User user : users) {
            db.userDao().insert(user);
        }
    }

    private void addUser() {
        String name = nameEditText.getText().toString();
        String job = jobEditText.getText().toString();

        if (!name.isEmpty() && !job.isEmpty()) {
            UserRequest newUserRequest = new UserRequest(name, job);
            Call<UserResponse> call = api.createUser(newUserRequest);

            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful()) {
                        UserResponse newUser = response.body();
                        if (newUser != null) {
                            User user = new User();
                            user.setName(newUser.getName());
                            user.setJob(newUser.getJob());
                            user.setId(newUser.getId());
                            db.userDao().insert(user);
                            userList.add(user);
                            userAdapter.notifyItemInserted(userList.size() - 1);
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Log.e("MainActivity", "API call failed", t);
                }
            });
        }
    }

//    private void updateUser() {
//        String name = nameEditText.getText().toString();
//        String job = jobEditText.getText().toString();
//
//        if (!name.isEmpty() && !job.isEmpty()) {
//            // Here you would typically get the user ID to update
//            // For this example, we'll assume you want to update the last user
//            if (!userList.isEmpty()) {
//                User userToUpdate = userList.get(userList.size() - 1); // Update the last user
//                UserRequest updatedUserRequest = new UserRequest(name, job);
//
//                Call<UserResponse> call = api.updateUser(userToUpdate.getId(), updatedUserRequest);
//                call.enqueue(new Callback<UserResponse>() {
//                    @Override
//                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
//                        if (response.isSuccessful()) {
//                            UserResponse updatedUser = response.body();
//                            if (updatedUser != null) {
//                                userToUpdate.setName(updatedUser.getName());
//                                userToUpdate.setJob(updatedUser.getJob());
//                                db.userDao().update(userToUpdate);
//                                userAdapter.notifyItemChanged(userList.indexOf(userToUpdate));
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<UserResponse> call, Throwable t) {
//                        Log.e("MainActivity", "API call failed", t);
//                    }
//                });
//            }
//        }
//    }

//    private void deleteUser() {
//        if (!userList.isEmpty()) {
//            // Here you would typically get the user ID to delete
//            // For this example, we'll assume you want to delete the last user
//            User userToDelete = userList.get(userList.size() - 1);
//
//            Call<Void> call = api.deleteUser(userToDelete.getId());
//            call.enqueue(new Callback<Void>() {
//                @Override
//                public void onResponse(Call<Void> call, Response<Void> response) {
//                    if (response.isSuccessful()) {
//                        db.userDao().delete(userToDelete);
//                        int position = userList.indexOf(userToDelete);
//                        userList.remove(position);
//                        userAdapter.notifyItemRemoved(position);
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Void> call, Throwable t) {
//                    Log.e("MainActivity", "API call failed", t);
//                }
//            });
//        }
//    }
}











//package com.example.usermanagementapp;
//
//import android.os.Bundle;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.usermanagementapp.data.AppDatabase;
//import com.example.usermanagementapp.data.User;
//import com.example.usermanagementapp.data.UserAdapter;
//import com.example.usermanagementapp.network.ApiClient;
//import com.example.usermanagementapp.network.ReqResApi;
//import com.example.usermanagementapp.network.UserResponse;
//import com.example.usermanagementapp.network.UserRequest;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Set up RecyclerView
//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Create an instance of the API interface
//        ReqResApi api = ApiClient.getRetrofitInstance().create(ReqResApi.class);
//
//        // Make the API call
//        Call<UserResponse> call = api.getUsers(2);
//        call.enqueue(new Callback<UserResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
//                if (response.isSuccessful()) {
//                    // Get the list of users from the response
//                    UserResponse userResponse = response.body();
//                    List<User> users = userResponse.getData();
//
//                    // Set up the RecyclerView adapter with the list of users
//                    UserAdapter adapter = new UserAdapter(users);
//                    recyclerView.setAdapter(adapter);
//                } else {
//                    Log.d("MainActivity", "Request failed");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<UserResponse> call, Throwable t) {
//                Log.e("MainActivity", "API call failed", t);
//            }
//        });
//        UserRequest newUser = new UserRequest("John Doe", "Developer");
//        Call<UserRequest> call2 = api.createUser(newUser);
//
//    }
//
//    // Example method to add a user (related to Room database)
//    public void addUser(User user) {
//        AppDatabase db = AppDatabase.getDatabase(this);
//        db.userDao().insert(user);
//        // Update RecyclerView (e.g., notify the adapter)
//    }
//
//    // Example method to delete a user (related to Room database)
//    public void deleteUser(User user) {
//        AppDatabase db = AppDatabase.getDatabase(this);
//        db.userDao().delete(user);
//        // Update RecyclerView (e.g., notify the adapter)
//    }
//}
//
//
////package com.example.usermanagementapp;
////
////import android.os.Bundle;
////import androidx.appcompat.app.AppCompatActivity;
////import com.example.usermanagementapp.data.AppDatabase;
////import com.example.usermanagementapp.data.User;
////
////public class MainActivity extends AppCompatActivity {
////
////    // Example method to add a user
////    public void addUser(User user) {
////        AppDatabase db = AppDatabase.getDatabase(this);  // 'this' is the context of MainActivity
////        db.userDao().insert(user);
////        // Update RecyclerView (e.g., notify the adapter)
////    }
////
////    // Example method to delete a user
////    public void deleteUser(User user) {
////        AppDatabase db = AppDatabase.getDatabase(this);  // 'this' is the context of MainActivity
////        db.userDao().delete(user);
////        // Update RecyclerView (e.g., notify the adapter)
////    }
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_main);
////    }
////}
