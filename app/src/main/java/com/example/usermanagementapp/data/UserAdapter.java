package com.example.usermanagementapp.data;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.usermanagementapp.R;
import com.example.usermanagementapp.ui.main.MainContract;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> users;
    private MainContract.Presenter presenter;
    private UpdateUserCallback updateUserCallback;

    // Constructor now initializes the user list if null
    public UserAdapter(MainContract.Presenter presenter, UpdateUserCallback updateUserCallback) {
        this.presenter = presenter;
        this.updateUserCallback = updateUserCallback;
        this.users = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);
        holder.textViewName.setText(user.getName());
        holder.textViewEmail.setText(user.getEmail());
        Glide.with(holder.itemView.getContext()).load(user.getAvatar()).into(holder.imageView);

        holder.buttonDelete.setOnClickListener(v -> {
            presenter.deleteUser(user);
        });

        holder.buttonUpdate.setOnClickListener(v -> {
            // Inflate the update form
            LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
            View formView = inflater.inflate(R.layout.dialog_update_user, null);

            // Set up the form fields with existing user data
            EditText firstNameEditText = formView.findViewById(R.id.editTextFirst_Name);
            EditText lastNameEditText = formView.findViewById(R.id.editTextLast_Name);
            EditText jobEditText = formView.findViewById(R.id.editTextJob);
            EditText emailEditText = formView.findViewById(R.id.editTextEmail);

            firstNameEditText.setText(user.getFirstName());
            lastNameEditText.setText(user.getLastName());
            jobEditText.setText(user.getJob());
            emailEditText.setText(user.getEmail());

            // Show the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setView(formView)
                    .setTitle("Update User")
                    .setPositiveButton("Save", (dialog, which) -> {
                        user.setFirstName(firstNameEditText.getText().toString());
                        user.setLastName(lastNameEditText.getText().toString());
                        user.setJob(jobEditText.getText().toString());
                        user.setEmail(emailEditText.getText().toString());

                        presenter.updateUser(user);
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // Method to set a new list of users
    @SuppressLint("NotifyDataSetChanged")
    public void setUserList(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
    // Method to add more users to the list
    public void addUsers(List<User> users) {
        int startPosition = this.users.size();
        this.users.addAll(users);
        notifyItemRangeInserted(startPosition, users.size());
    }

    public interface UpdateUserCallback {////////////////////////
        void onUpdateUserClicked(User user);
    }

    // Method to get the last user in the list
    public User getLastUser() {
        if (users != null && !users.isEmpty()) {
            return users.get(users.size() - 1);
        }
        return null;
    }

    // Method to add a new user to the list
    public void addUser(User user) {
        users.add(user);
        notifyItemInserted(users.size()-1);
    }

    // Method to remove a user from the list
    public void removeUser(User user) {
        int position = users.indexOf(user);
        if (position >= 0) {
            users.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateUser(User user) {
        int position = users.indexOf(user);
        if (position >= 0) {
            users.set(position, user);
            notifyItemChanged(position);
        }
    }
    public int getUserPosition(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                return i;
            }
        }
        return -1;
    }

    public void updateUser(int position, User user) {
        users.set(position, user);
        notifyItemChanged(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public ImageView imageView;
        public TextView textViewEmail;
        public Button buttonUpdate;
        public Button buttonDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            imageView = itemView.findViewById(R.id.imageView);
            buttonUpdate = itemView.findViewById(R.id.buttonUpdate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
