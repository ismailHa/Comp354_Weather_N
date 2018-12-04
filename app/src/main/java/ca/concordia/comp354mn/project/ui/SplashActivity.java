package ca.concordia.comp354mn.project.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ca.concordia.comp354mn.project.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;

/**
 * Displays a splash screen on startup and directs to the correct place
 */

public class SplashActivity extends AppCompatActivity implements View.OnClickListener  {

    private SharedPreferences prefs;
    private Resources res;
    private GoogleSignInClient  _client;
    private GoogleSignInAccount _account;

    // Interactive elements
    private TextView     tv_greeting;
    private SignInButton btn_gmsSignIn;
    private Button       btn_setupProfile;

    @Override
    protected void onStart() {
        super.onStart();

        // Check if we're already logged in
        _account = GoogleSignIn.getLastSignedInAccount(this);

        if(_account == null) {
            // User hasn't logged in before. Take them through the initial setup.

            // Show Google sign in
            btn_gmsSignIn.setOnClickListener(this);
            btn_gmsSignIn.setVisibility(View.VISIBLE);

            // Ask for user to sign in
            tv_greeting.setText(R.string.do_gms_setup);
            tv_greeting.setVisibility(View.VISIBLE);

        } else {
            continueStartup();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 9001) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.

            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setVisibility(View.INVISIBLE);
            continueStartup();
        } catch (ApiException e) {
            Log.e("COMP354N", "signInResult:failed code=" + e.getStatusCode());
            Log.e("COMP354N",e.getMessage());
        }

    }

    private void continueStartup() {

        // User profile setup complete, greet user and go to main activity
        if (prefs.contains("user_name") && (!prefs.getString("user_name","").isEmpty())) {
            String firstName = prefs.getString("user_name", "").split("\\s+")[0];
            String greetingText = String.format(res.getString(R.string.welcome_back_user), firstName);
            tv_greeting.setText(greetingText);
            tv_greeting.setVisibility(View.VISIBLE);

            // Delay for 1s on loading
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                }
            }, 1000);

        } else { // User has logged in to Google but hasn't saved their profile data yet.

            btn_setupProfile.setOnClickListener(this);
            btn_setupProfile.setVisibility(View.VISIBLE);

            // Ask for user to sign in
            tv_greeting.setText(R.string.do_profile_setup);
            tv_greeting.setVisibility(View.VISIBLE);

//            Intent createProfileIntent = new Intent(SplashActivity.this, SplashCreateProfileActivity.class);
//            SplashActivity.this.startActivity(createProfileIntent);
//            SplashActivity.this.finish();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = _client.getSignInIntent();
                startActivityForResult(signInIntent, 9001);
                break;
            case R.id.setup_profile:
                Intent createProfileIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                createProfileIntent.putExtra( SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.UserPreferenceFragment.class.getName() );
                createProfileIntent.putExtra( SettingsActivity.EXTRA_NO_HEADERS, true );
                this.startActivity(createProfileIntent);
                this.finish();
                break;
        }
    }


    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        res = getResources();
        setContentView(R.layout.activity_splash);

        tv_greeting      = (TextView) findViewById(R.id.textView_splash);
        btn_gmsSignIn    = (SignInButton) findViewById(R.id.sign_in_button);
        btn_setupProfile = (Button) findViewById(R.id.setup_profile);

        // Try to sign in to Google account.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestScopes(Drive.SCOPE_FILE)
                .requestEmail()
                .build();


        _client = GoogleSignIn.getClient(this,gso);

    }

}


