package tr.duzce.edu.bm.androidquoteapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import tr.duzce.edu.bm.androidquoteapp.models.User;

@Dao
public interface UserDao {
    @Insert
    void registerUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Update
    void updateUser(User user);
    
    @Query("UPDATE users SET password = :newHashedPassword WHERE email = :email")
    void updatePassword(String email, String newHashedPassword);

    @Query("DELETE FROM users WHERE email = :email")
    void deleteUserByEmail(String email);
}
