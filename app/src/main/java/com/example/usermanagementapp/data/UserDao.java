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

    @Insert
    void insertAll(List<User> users);

    @Query("SELECT * FROM User")
    List<User> getAllUsers();

    @Query("SELECT * FROM User WHERE id = :id LIMIT 1")
    User getUserById(int id);

    @Delete
    void delete(User user);

    @Update
    void update(User user);

    @Query("DELETE FROM User")
    void clearAll();

    @Query("SELECT * FROM User WHERE first_name = :name LIMIT 1")
    User getUserByName(String name);
}