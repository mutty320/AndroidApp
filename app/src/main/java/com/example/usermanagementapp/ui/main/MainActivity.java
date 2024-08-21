package com.example.usermanagementapp.ui.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getDatabase(this);

        presenter = new MainPresenter(this, db);

        userAdapter = new UserAdapter(presenter);
        recyclerView.setAdapter(userAdapter);

        // Clear database in the background
        clearDatabase(db);

        presenter.loadUsers();

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String job = jobEditText.getText().toString();
            if (!name.isEmpty() && !job.isEmpty()) {
                presenter.addUser(name, job);
            } else {
                showError("Please provide both name and job");
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
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
