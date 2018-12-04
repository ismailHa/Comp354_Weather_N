package ca.concordia.comp354mn.project.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import ca.concordia.comp354mn.project.R;
import ca.concordia.comp354mn.project.enums.WeatherKey;
import ca.concordia.comp354mn.project.network.DarkskyWeatherProvider;
import ca.concordia.comp354mn.project.parsing.JsonDataParser;
import ca.concordia.comp354mn.project.utils.App;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.widget.DataBufferAdapter;
import com.google.android.gms.tasks.*;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GDriveStorage extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_FILE = 2;

    Context context;
    GoogleSignInClient _client;
    GoogleSignInOptions _options;
    GoogleSignInAccount _account;

    DriveClient _driveClient;
    DriveResourceClient _driveResourceClient;

    final String TAG = "COMP354::GDriveStorage";

    public GDriveStorage() {
        context = App.getAppContext();

        // Try to sign in to Google account.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build();

        _client = GoogleSignIn.getClient(context,gso);

        // Check if we're already logged in
        _account = GoogleSignIn.getLastSignedInAccount(context);
        if(_account != null) {
            _driveClient = Drive.getDriveClient(context, _account);
            _driveResourceClient = Drive.getDriveResourceClient(context, _account);
        }

    }

    /**
     * Write out stringified file to Google Drive.
     * Just dumps it in the root folder for right now
     * @param filename
     * @param content
     */
    public void write(final String filename, final String content) {
        final Task<DriveFolder> rootFolderTask = _driveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = _driveResourceClient.createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = rootFolderTask.getResult();
                        DriveContents existingContents = createContentsTask.getResult();
                        OutputStream outputStream = existingContents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write(content);
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(filename)
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();
                        return _driveResourceClient.createFile(parent, changeSet, existingContents);
                    }
                }).addOnSuccessListener(this,
                new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        Log.i(TAG, "Saved file " + filename + " to Google Drive.");
                        finish();
                    }
                }).addOnFailureListener(this,
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        finish();
                    }
                }
        );
    }


    public ArrayList<String> getFileList(String extension) {
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, extension))
                .build();

        Task<MetadataBuffer> queryTask = _driveResourceClient.query(query);


        ArrayList<String> l = new ArrayList<String>();

        try {
            MetadataBuffer buffer = Tasks.await(queryTask);

            for(Metadata m : buffer) {
                l.add(m.getOriginalFilename());
            }


        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }

        return l;
    }

    public void contains(final String filename) {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, filename))
                .build();

        Task<MetadataBuffer> queryTask = _driveResourceClient.query(query);
        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer buffer) {
                                if(buffer.getCount() == 0) {
                                    Log.e(TAG,"File " + filename + "not found");
                                } else {
                                    Log.e(TAG,"File " + filename + "present");
                                }
                            }
                        });

    }


}
