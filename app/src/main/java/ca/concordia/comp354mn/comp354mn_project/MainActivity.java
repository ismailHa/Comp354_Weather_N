package ca.concordia.comp354mn.comp354mn_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.preference.PreferenceManager;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    boolean showedSplashScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        System.out.println(showedSplashScreen);

//        if(!showedSplashScreen) {
//            Intent intent = new Intent(this,SplashActivity.class);
//            showedSplashScreen = true;
//
//            startActivity(intent);
//        }
        setContentView(R.layout.activity_main);








//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

//        if(prefs.contains("user_name")) {
//            setContentView(R.layout.activity_splash);
//            Resources res = getResources();
//            View textViewId = findViewById(R.id.textView_splash);
//            TextView tv1 = (TextView)textViewId;
//
//            String firstName = prefs.getString("user_name","").split("\\s+")[0];
//            String greetingText = String.format(res.getString(R.string.welcome_back_user), firstName);
//
//            tv1.setText(greetingText);
//
//        } else {
//            setContentView(R.layout.activity_splash_create_profile_prompt);
//        }
//
    }

}
