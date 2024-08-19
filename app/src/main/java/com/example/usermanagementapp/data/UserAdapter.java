package com.example.usermanagementapp.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.usermanagementapp.R;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> users;

    // Constructor now initializes the user list if null
    public UserAdapter() {
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
        Glide.with(holder.itemView.getContext()).load(user.getAvatar()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // Method to set a new list of users
    public void setUserList(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
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
        notifyItemInserted(users.size() - 1);
    }

    // Method to remove a user from the list
    public void removeUser(User user) {
        int position = users.indexOf(user);
        if (position >= 0) {
            users.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
