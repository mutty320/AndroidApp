package com.example.usermanagementapp.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usermanagementapp.R;
import com.example.usermanagementapp.data.AppDatabase;
import com.example.usermanagementapp.data.User;
import com.example.usermanagementapp.data.UserAdapter;
import android.view.LayoutInflater;

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

        userAdapter = new UserAdapter(presenter, this::showUpdateForm);

        recyclerView.setAdapter(userAdapter);

        // Clear database in the background
        clearDatabase(db);

        presenter.loadUsers();

//        Button addButton = findViewById(R.id.addButton);
//        addButton.setOnClickListener(v -> showAddUserForm());

    }

//    private void showAddUserForm() {
//        // Inflate the add user form
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View formView = inflater.inflate(R.layout.dialog_update_user, null);
//
//        // Set up the form fields
//        EditText firstNameEditText = formView.findViewById(R.id.editTextFirst_Name);
//        EditText lastNameEditText = formView.findViewById(R.id.editTextLast_Name);
//        EditText jobEditText = formView.findViewById(R.id.editTextJob);
//        EditText emailEditText = formView.findViewById(R.id.editTextEmail);
//
//        // Show the dialog
//        new AlertDialog.Builder(this)
//                .setTitle("Add User")
//                .setView(formView)
//                .setPositiveButton("Save", (dialog, which) -> {
//                    // Collect the user input
//                    String firstName = firstNameEditText.getText().toString();
//                    String lastName = lastNameEditText.getText().toString();
//                    String job = jobEditText.getText().toString();
//                    String email = emailEditText.getText().toString();
//
//                    // Create a new user and call the presenter to add it
//                    User newUser = new User();
//                    newUser.setFirstName(firstName);
//                    newUser.setLastName(lastName);
//                    newUser.setJob(job);
//                    newUser.setEmail(email);
//
//                    presenter.addUser(newUser);
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }

    private void showUpdateForm(User user) {
        View formView = getLayoutInflater().inflate(R.layout.dialog_update_user, null);
        EditText updateFirstNameEditText = formView.findViewById(R.id.editTextFirst_Name);
        EditText updateLastNameEditText = formView.findViewById(R.id.editTextLast_Name);
        EditText updateJobEditText = formView.findViewById(R.id.editTextJob);
        EditText updateEmailEditText = formView.findViewById(R.id.editTextEmail);

        updateFirstNameEditText.setText(user.getFirstName());
        updateLastNameEditText.setText(user.getLastName());
        updateJobEditText.setText(user.getJob());
        updateEmailEditText.setText(user.getEmail());

        new AlertDialog.Builder(this)
                .setTitle("Update User")
                .setView(formView)
                .setPositiveButton("Save", (dialog, which) -> {
                    user.setFirstName(updateFirstNameEditText.getText().toString());
                    user.setLastName(updateLastNameEditText.getText().toString());
                    user.setJob(updateJobEditText.getText().toString());
                    user.setEmail(updateEmailEditText.getText().toString());

                    presenter.updateUser(user);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void clearDatabase(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(db.userDao()::clearAll);
    }

    @Override
    public void showUserUpdated(User user) {
        runOnUiThread(() -> {
            int position = userAdapter.getUserPosition(user);
            if (position >= 0) {
                userAdapter.updateUser(position, user);
            }
        });
    }


//    @Override
//    public void showUsers(List<User> users) {
//        runOnUiThread(() -> userAdapter.setUserList(users));
//    }
@Override
public void showUsers(List<User> users) {
    if (userAdapter != null) {
        runOnUiThread(() -> {
            userAdapter.setUserList(users);
            userAdapter.notifyDataSetChanged();
        });
    }
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
