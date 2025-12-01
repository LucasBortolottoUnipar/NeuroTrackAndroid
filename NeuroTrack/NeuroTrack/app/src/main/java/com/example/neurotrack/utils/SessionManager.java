package com.example.neurotrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    
    private static final String PREF_NAME = "NeuroTrackSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    

    public void createLoginSession(String token, Long userId, String role) {
        editor.putString(KEY_TOKEN, token);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    

    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        if (token != null) {
            return "Bearer " + token;
        }
        return null;
    }

    public String getRawToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public Long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isGuardian() {
        return "GUARDIAN".equals(getRole());
    }

    public boolean isChild() {
        return "CHILD".equals(getRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}

