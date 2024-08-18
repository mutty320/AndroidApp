package com.example.usermanagementapp.data;

//DAO = (Data Access Object):
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM User")
    List<User> getAllUsers();

    @Delete
    void delete(User user);

    @Update
    void update(User user);
}