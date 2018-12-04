package ca.concordia.comp354mn.project.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;
import ca.concordia.comp354mn.project.R;
import ca.concordia.comp354mn.project.utils.App;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;

public class GoogleLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private GoogleSignInClient _client;
    private GoogleSignInAccount _account;

    // Interactive elements
    private TextView tv_warning;
    private SignInButton btn_gmsSignIn;
    private CheckedTextView ctv_signedIn;

    @Override
    protected void onStart() {
        super.onStart();

        // Check if we're already logged in
        _account = GoogleSignIn.getLastSignedInAccount(this);

        if (_account == null) {
            // User hasn't logged in before. Take them through the initial setup.

            // Show Google sign in
            btn_gmsSignIn.setOnClickListener(this);
            btn_gmsSignIn.setVisibility(View.VISIBLE);
        } else {

            btn_gmsSignIn.setVisibility(View.INVISIBLE);
            tv_warning.setVisibility(View.INVISIBLE);
            ctv_signedIn.setVisibility(View.VISIBLE);
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

            btn_gmsSignIn.setVisibility(View.INVISIBLE);
            tv_warning.setVisibility(View.INVISIBLE);
            ctv_signedIn.setVisibility(View.VISIBLE);


        } catch (ApiException e) {
            Toast.makeText(App.getAppContext(),"Sign in failed. Make sure you have added yourself to API console.",Toast.LENGTH_LONG).show();
            Log.e("COMP354N", "signInResult:failed code=" + e.getStatusCode());
            Log.e("COMP354N",e.getMessage());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = _client.getSignInIntent();
                startActivityForResult(signInIntent, 9001);
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        btn_gmsSignIn = findViewById(R.id.sign_in_button);
        tv_warning = findViewById(R.id.google_warning);
        ctv_signedIn = findViewById(R.id.ctv_signedInTrue);

        // Try to sign in to Google account.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestScopes(Drive.SCOPE_FILE)
                .requestEmail()
                .build();


        _client = GoogleSignIn.getClient(this,gso);

    }
}
