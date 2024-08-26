package com.example.usermanagementapp.data;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
            updateUserCallback.onUpdateUserClicked(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUserList(List<User> users) {
        this.users.clear();  // Clear the existing users before setting the new list
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    public void addUsers(List<User> users) {
        int startPosition = this.users.size();
        this.users.addAll(users);
        notifyItemRangeInserted(startPosition, users.size());
    }

    public void addUser(User user) {
        users.add(user);
        notifyItemInserted(users.size() - 1);
    }

    public void removeUser(User user) {
        int position = users.indexOf(user);
        if (position >= 0) {
            users.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clearAllUsers() {
        users.clear();
        notifyDataSetChanged();
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

    public interface UpdateUserCallback {
        void onUpdateUserClicked(User user);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public ImageView imageView;
        public TextView textViewEmail;
        public ImageButton buttonUpdate;
        public ImageButton buttonDelete;

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
