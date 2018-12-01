package ca.concordia.comp354mn.project.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import ca.concordia.comp354mn.project.R;

/**
 * Displays a splash screen on startup and directs to the correct place
 */

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();
        setContentView(R.layout.activity_splash);
        View textViewId = findViewById(R.id.textView_splash);
        TextView tv1 = (TextView) textViewId;
        final boolean userNameExists = prefs.contains("user_name");
        Intent nextIntent;

        if(userNameExists) {
            String firstName = prefs.getString("user_name", "").split("\\s+")[0];
            String greetingText = String.format(res.getString(R.string.welcome_back_user), firstName);
            tv1.setText(greetingText);
        } else {
            tv1.setText(res.getString(R.string.welcome_user));
        }


        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if(userNameExists) {
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                } else {
                    Intent createProfileIntent = new Intent(SplashActivity.this, SplashCreateProfileActivity.class);
                    SplashActivity.this.startActivity(createProfileIntent);
                    SplashActivity.this.finish();
                }
            }
        }, 1000);

    }

}
