package tr.duzce.edu.bm.androidquoteapp.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String email;
    private String password; // Will store the hashed password
    private boolean isValidated;
    private String verificationToken;
    private long tokenExpiryTimestamp; // For security: Token expiration

    public User(String email, String password, boolean isValidated) {
        this.email = email;
        this.password = password;
        this.isValidated = isValidated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean validated) {
        isValidated = validated;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public long getTokenExpiryTimestamp() {
        return tokenExpiryTimestamp;
    }

    public void setTokenExpiryTimestamp(long tokenExpiryTimestamp) {
        this.tokenExpiryTimestamp = tokenExpiryTimestamp;
    }
}
