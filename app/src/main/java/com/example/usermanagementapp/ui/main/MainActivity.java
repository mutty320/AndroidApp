package com.example.usermanagementapp.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usermanagementapp.R;
import com.example.usermanagementapp.data.AppDatabase;
import com.example.usermanagementapp.data.User;
import com.example.usermanagementapp.data.UserAdapter;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private EditText nameEditText, jobEditText;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private MainContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        jobEditText = findViewById(R.id.jobEditText);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(); // This remains in the `data` package
        recyclerView.setAdapter(userAdapter);

        // Initialize Presenter
        AppDatabase db = AppDatabase.getDatabase(this);

        // Clear the database on startup
        clearDatabase(db);

        presenter = new MainPresenter(this, db);

        // Load initial users
        presenter.loadUsers();

        // Set up button click listeners
        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String job = jobEditText.getText().toString();
            presenter.addUser(name, job);
        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            User lastUser = userAdapter.getLastUser();
            if (lastUser != null) {
                presenter.deleteUser(lastUser);
            }
        });
    }

    private void clearDatabase(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(db.userDao()::clearAll);
    }

    @Override
    public void showUsers(List<User> users) {
        runOnUiThread(() -> userAdapter.setUserList(users));
    }

    @Override
    public void showUserAdded(User user) {
        runOnUiThread(() -> userAdapter.addUser(user));
    }

    @Override
    public void showUserDeleted(User user) {
        runOnUiThread(() -> userAdapter.removeUser(user));
    }

    @Override
    public void showError(String message) {
        // Handle error (e.g., show a Toast or Snackbar)
        runOnUiThread(() -> {
            // Code to display error message to the user
        });
    }
}
