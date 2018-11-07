package ca.concordia.comp354mn.comp354mn_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


/**
 * Prompts user to setup new acct
 */

public class SplashCreateProfileActivity extends Activity {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash_create_profile_prompt);

    }

    public void setupProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }
}

