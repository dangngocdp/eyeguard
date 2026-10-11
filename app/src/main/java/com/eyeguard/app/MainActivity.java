package com.eyeguard.app;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Switch;
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch sw = findViewById(R.id.switchEnable);
        sw.setChecked(true);
        startForegroundService(new Intent(this, EyeGuardService.class));
        sw.setOnCheckedChangeListener((b, on) -> {
            if (on) startForegroundService(new Intent(this, EyeGuardService.class));
            else stopService(new Intent(this, EyeGuardService.class));
        });
    }
}
