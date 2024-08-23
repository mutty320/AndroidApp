package com.example.usermanagementapp.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private int currentPage = 1;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getDatabase(this);

        sharedPreferences = getSharedPreferences("UserManagementAppPrefs", MODE_PRIVATE);
        presenter = new MainPresenter(this, db, sharedPreferences);

        userAdapter = new UserAdapter(presenter, this::showUpdateForm);

        recyclerView.setAdapter(userAdapter);
        presenter.loadUsers(currentPage);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == userAdapter.getItemCount() - 1) {
                    // Load the next page
                    presenter.loadUsers(++currentPage);
                }
            }
        });

        Button addButton = findViewById(R.id.addButton);
       addButton.setOnClickListener(v -> showAddUserForm());

    }

    private void showAddUserForm() {
        // Inflate the add user form
        LayoutInflater inflater = LayoutInflater.from(this);
        View formView = inflater.inflate(R.layout.dialog_update_user, null);

        // Set up the form fields
        EditText firstNameEditText = formView.findViewById(R.id.editTextFirst_Name);
        EditText lastNameEditText = formView.findViewById(R.id.editTextLast_Name);
        EditText jobEditText = formView.findViewById(R.id.editTextJob);
        EditText emailEditText = formView.findViewById(R.id.editTextEmail);

        // Show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add User")
                .setView(formView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                // Validate form before proceeding
                if (validateForm(firstNameEditText, lastNameEditText, jobEditText, emailEditText)) {
                    // Collect the user input
                    String firstName = firstNameEditText.getText().toString();
                    String lastName = lastNameEditText.getText().toString();
                    String job = jobEditText.getText().toString();
                    String email = emailEditText.getText().toString();

                    // Create a new user and call the presenter to add it
                    User newUser = new User();
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setJob(job);
                    newUser.setEmail(email);

                    presenter.addUser(newUser);
                    Toast.makeText(MainActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();

                    // Close the dialog
                    dialog.dismiss();
                } else {
                    // Show a general error message if validation fails
                    Toast.makeText(MainActivity.this, "Please correct the errors", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showUpdateForm(User user) {
        // Inflate the update user form
        View formView = getLayoutInflater().inflate(R.layout.dialog_update_user, null);
        EditText updateFirstNameEditText = formView.findViewById(R.id.editTextFirst_Name);
        EditText updateLastNameEditText = formView.findViewById(R.id.editTextLast_Name);
        EditText updateJobEditText = formView.findViewById(R.id.editTextJob);
        EditText updateEmailEditText = formView.findViewById(R.id.editTextEmail);

        updateFirstNameEditText.setText(user.getFirstName());
        updateLastNameEditText.setText(user.getLastName());
        updateJobEditText.setText(user.getJob());
        updateEmailEditText.setText(user.getEmail());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Update User")
                .setView(formView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                // Validate form before proceeding
                if (validateForm(updateFirstNameEditText, updateLastNameEditText, updateJobEditText, updateEmailEditText)) {
                    // Update the user with validated input
                    user.setFirstName(updateFirstNameEditText.getText().toString());
                    user.setLastName(updateLastNameEditText.getText().toString());
                    user.setJob(updateJobEditText.getText().toString());
                    user.setEmail(updateEmailEditText.getText().toString());

                    presenter.updateUser(user);
                    Toast.makeText(MainActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();

                    // Close the dialog
                    dialog.dismiss();
                } else {
                    // Show a general error message if validation fails
                    Toast.makeText(MainActivity.this, "Please correct the errors", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
    
    private boolean validateForm(EditText firstNameEditText, EditText lastNameEditText, EditText jobEditText, EditText emailEditText) {
        boolean isValid = true;

        // Validate first name
        if (firstNameEditText.getText().toString().trim().isEmpty()) {
            firstNameEditText.setError("First Name is required");
            isValid = false;
        } else if (firstNameEditText.getText().toString().length() > 50) {
            firstNameEditText.setError("First Name is too long");
            isValid = false;
        }

        // Validate last name
        if (lastNameEditText.getText().toString().trim().isEmpty()) {
            lastNameEditText.setError("Last Name is required");
            isValid = false;
        } else if (lastNameEditText.getText().toString().length() > 50) {
            lastNameEditText.setError("Last Name is too long");
            isValid = false;
        }

        // Validate job
        if (jobEditText.getText().toString().trim().isEmpty()) {
            jobEditText.setError("Job is required");
            isValid = false;
        } else if (jobEditText.getText().toString().length() > 50) {
            jobEditText.setError("Job is too long");
            isValid = false;
        }

        // Validate email
        if (emailEditText.getText().toString().trim().isEmpty()) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
            emailEditText.setError("Invalid email format");
            isValid = false;
        }

        return isValid;
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

    @Override
    public void showUsers(List<User> users) {
        if (userAdapter != null) {
            runOnUiThread(() -> {
                userAdapter.setUserList(users);
                userAdapter.notifyItemRangeChanged(0, users.size());
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
