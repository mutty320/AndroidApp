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

    @Query("SELECT * FROM User WHERE id = :id LIMIT 1")
    User getUserById(int id);

    @Delete
    void delete(User user);

    @Update
    void update(User user);

    @Query("DELETE FROM User")
    void clearAll();

    @Query("SELECT * FROM User LIMIT :pageSize OFFSET :offset")
    List<User> getUsersByPage(int offset, int pageSize);

    @Query("SELECT * FROM User WHERE firstName = :firstName AND lastName = :lastName LIMIT 1")
    User getUserByFullName(String firstName, String lastName);

    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

}