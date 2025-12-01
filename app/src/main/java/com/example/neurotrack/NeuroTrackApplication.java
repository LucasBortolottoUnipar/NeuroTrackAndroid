package com.example.neurotrack;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class NeuroTrackApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
    }
}

