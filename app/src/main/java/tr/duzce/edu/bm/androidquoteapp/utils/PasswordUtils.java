package tr.duzce.edu.bm.androidquoteapp.utils;

import android.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {
    // Emulator performansı için iterasyon sayısı optimize edildi
    private static final int ITERATIONS = 2000; 
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String hashPassword(String password) {
        if (password == null) return null;
        char[] passwordChars = password.toCharArray();
        byte[] salt = getSalt();
        byte[] hash = hash(passwordChars, salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" + Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) return false;
        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] hash = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] testHash = hash(password.toCharArray(), salt);
            
            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Hashing error", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] getSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
}
