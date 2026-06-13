package io.alain.shorthander;
import io.alain.shorthander.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedStorage {
    private static final String PREFS_FILENAME = "shorthander_secure_prefs";
    private static final String TAG = "ShorthanderCrypto";

    public static SharedPreferences getPrefs(Context context) {
        try {
            // Initialize or retrieve the hardware-backed AES256 master key
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Return the encrypted key-value storage instance
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILENAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Cryptographic initialization failure. Falling back.", e);
            // Fallback gracefully to standard storage context if hardware tokens fail
            return context.getSharedPreferences("shorthander_fallback_prefs", Context.MODE_PRIVATE);
        }
    }

    public static void write(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    public static String read(Context context, String key) {
        return getPrefs(context).getString(key, "");
    }
}