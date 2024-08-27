package com.example.usermanagementapp.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usermanagementapp.R;
import com.example.usermanagementapp.data.AppDatabase;
import com.example.usermanagementapp.data.User;
import com.example.usermanagementapp.data.UserAdapter;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private MainContract.Presenter presenter;
    private LinearLayoutManager layoutManager;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isResetting = false;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getDatabase(this);

        SharedPreferences sharedPreferences = getSharedPreferences("UserManagementAppPrefs", MODE_PRIVATE);
        presenter = new MainPresenter(this, db, sharedPreferences);

        userAdapter = new UserAdapter(presenter, this::showUpdateForm);

        recyclerView.setAdapter(userAdapter);
        setupScrollListener();

        presenter.loadUsers(currentPage);
    }

    private void showAddUserForm() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View formView = inflater.inflate(R.layout.dialog_update_user, null);

        EditText firstNameEditText = formView.findViewById(R.id.editTextFirst_Name);
        EditText lastNameEditText = formView.findViewById(R.id.editTextLast_Name);
        EditText jobEditText = formView.findViewById(R.id.editTextJob);
        EditText emailEditText = formView.findViewById(R.id.editTextEmail);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add User")
                .setView(formView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {

                if (validateForm(firstNameEditText, lastNameEditText, jobEditText, emailEditText)) {

                    String firstName = firstNameEditText.getText().toString();
                    String lastName = lastNameEditText.getText().toString();
                    String job = jobEditText.getText().toString();
                    String email = emailEditText.getText().toString();

                    User newUser = new User();
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setJob(job);
                    newUser.setEmail(email);

                    presenter.addUser(newUser);

                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please correct the errors", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

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

                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please correct the errors", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private boolean validateForm(EditText firstNameEditText, EditText lastNameEditText, EditText jobEditText, EditText emailEditText) {
        boolean isValid = true;

        if (firstNameEditText.getText().toString().trim().isEmpty()) {
            firstNameEditText.setError("First Name is required");
            isValid = false;
        } else if (firstNameEditText.getText().toString().length() > 50) {
            firstNameEditText.setError("First Name is too long");
            isValid = false;
        }

        if (lastNameEditText.getText().toString().trim().isEmpty()) {
            lastNameEditText.setError("Last Name is required");
            isValid = false;
        } else if (lastNameEditText.getText().toString().length() > 50) {
            lastNameEditText.setError("Last Name is too long");
            isValid = false;
        }

        if (jobEditText.getText().toString().trim().isEmpty()) {
            jobEditText.setError("Job is required");
            isValid = false;
        } else if (jobEditText.getText().toString().length() > 50) {
            jobEditText.setError("Job is too long");
            isValid = false;
        }

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

    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1) && !isLoading && !isResetting) {
                    currentPage++;
                    isLoading = true;
                    presenter.loadUsers(currentPage);
                }
            }
        });
    }

    @Override
    public void showUsers(List<User> users) {
        runOnUiThread(() -> {
            if (isResetting) {
                userAdapter.setUserList(users);  // Clear and set the list on reset
                isResetting = false;
            } else {
                if (currentPage == 1) {
                    userAdapter.setUserList(users);  // Clear and set the list on the first page
                } else {
                    userAdapter.addUsers(users);  // Add users for subsequent pages
                }
            }
            isLoading = false;
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            showSearchUserDialog();

            return true;
        } else if (id == R.id.action_clear_all) {
            clearAllUsers();
            showClearAllUsers();
            return true;
        } else if (id == R.id.action_add) {
            showAddUserForm();
            return true;
        } else if (id == R.id.action_back){
            isResetting = true;
            currentPage = 1;
            userAdapter.setUserList(new ArrayList<>());  // Clear the adapter
            presenter.loadUsers(currentPage);  // Load the first page of users
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showSearchUserDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View formView = inflater.inflate(R.layout.dialog_search_user, null);

        EditText searchNameEditText = formView.findViewById(R.id.searchNameEditText);
        EditText searchEmailEditText = formView.findViewById(R.id.searchEmailEditText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Search User")
                .setView(formView)
                .setPositiveButton("Search", (d, which) -> {
                    // Perform the search
                    String name = searchNameEditText.getText().toString().trim();
                    String email = searchEmailEditText.getText().toString().trim();

                    if (!name.isEmpty()) {
                        searchUserByName(name);
                    } else if (!email.isEmpty()) {
                        searchUserByEmail(email);
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a name or email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }

    private void searchUserByName(String name) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String[] parts = name.split(" ");
            if (parts.length == 2) {
                User user = db.userDao().getUserByFullName(parts[0], parts[1]);
                runOnUiThread(() -> showSearchResult(user));
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Please enter a full name", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void searchUserByEmail(String email) {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().getUserByEmail(email);
            runOnUiThread(() -> showSearchResult(user));
        });
    }

    private void showSearchResult(User user) {
        if (user != null) {
            userAdapter.setUserList(Collections.singletonList(user));
        } else {
            Toast.makeText(MainActivity.this, "No user found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showClearAllUsers() {
        runOnUiThread(() -> {
            userAdapter.clearAllUsers(); // Clear the RecyclerView in the adapter
        });
    }
    private void clearAllUsers() {
        presenter.clearAllUsers(); // Trigger the clear action in the presenter
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
