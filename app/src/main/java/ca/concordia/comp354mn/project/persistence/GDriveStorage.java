package ca.concordia.comp354mn.project.persistence;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.DeadObjectException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.AndroidException;
import android.util.Log;
import android.widget.Toast;
import ca.concordia.comp354mn.project.ui.MainActivity;
import ca.concordia.comp354mn.project.utils.App;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.*;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class GDriveStorage extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_FILE = 0;
    private static final String TAG = "GDriveStorage";

    private final Context context;
    GoogleSignInClient _client;
    GoogleSignInOptions _options;
    GoogleSignInAccount _account;

    DriveClient _driveClient;
    DriveResourceClient _driveResourceClient;
    ArrayList<String> files;

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

        // Initialize list of files to be returned
        files = new ArrayList<>();

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

    public void getFilesAsync(final MainActivity a) {
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, ".json"))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .build();

        try {
            if ((_driveClient == null) || (_driveResourceClient == null)) {
                Log.e(TAG, "Drive object not initialized");
                throw new ExecutionException("No Google login!", new AndroidException());
            }
            Task<MetadataBuffer> queryTask = _driveResourceClient.query(query);

            queryTask.continueWith(new Continuation<MetadataBuffer, ArrayList<Task<DriveContents>>>() {
                @Override
                public ArrayList<Task<DriveContents>> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                    MetadataBuffer mb = task.getResult();
                    ArrayList<Task<DriveContents>> list = new ArrayList<>();
                    for (Metadata m : mb) {
                        Task<DriveContents> openFileTask = _driveResourceClient.openFile(m.getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);
                        openFileTask.addOnCompleteListener(new OnCompleteListener<DriveContents>() {
                            @Override
                            public void onComplete(@NonNull Task<DriveContents> task) {
                                DriveContents d = task.getResult();
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(d.getInputStream()))) {
                                    StringBuilder builder = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                    a.addImportedData(builder.toString());

                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });

                        openFileTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Unable to read contents", e);
                            }
                        });
                        list.add(openFileTask);
                    }
                    return list;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

//        queryTask.continueWithTask(new Continuation<MetadataBuffer, Task<DriveContents>>() {
//            @Override
//            public Task<DriveContents> then(@NonNull Task<MetadataBuffer> task) throws Exception {
//                MetadataBuffer mb = task.getResult();
//                Task<DriveContents> openFileTask;
//                for(Metadata m : mb){
//                    openFileTask = _driveResourceClient.openFile(m.getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);
//
//                    openFileTask.addOnCompleteListener(new OnCompleteListener<DriveContents>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DriveContents> task) {
//                            DriveContents d = task.getResult();
//                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(d.getInputStream()))) {
//                                StringBuilder builder = new StringBuilder();
//                                String line;
//                                while ((line = reader.readLine()) != null) {
//                                    builder.append(line);
//                                }
//                                a.listImported(builder.toString());
//
//                            } catch (Exception e) {
//                                Log.e(TAG, e.getMessage());
//                            }
//                        }
//                    });
//
//                    openFileTask.addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.e(TAG, "Unable to read contents", e);
//                        }
//                    });
//
//                }
//                return openFileTask;
//            }
//        });
//    }

//            MetadataBuffer mb = Tasks.await(queryTask);
//            ArrayList<DriveFile> files = new ArrayList<>();
//            for (Metadata m : mb) {
//                files.add(m.getDriv/eId().asDriveFile());
//            }
//
//                Task<DriveContents> openFileTask = _driveResourceClient.openFile(m.getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);
//                openFileTask.addOnCompleteListener(new OnCompleteListener<DriveContents>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DriveContents> task) {
//                        DriveContents d = task.getResult();
//                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(d.getInputStream()))) {
//                            StringBuilder builder = new StringBuilder();
//                            String line;
//                            while ((line = reader.readLine()) != null) {
//                                builder.append(line);
//                            }
//                            a.addToCache(builder.toString());
//
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                        }
//                    }
//                });
//
//                openFileTask.addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "Unable to read contents", e);
//                    }
//                });
//
//            }
//        } catch (Exception e) {
//            Log.e(TAG,e.getMessage());
//            e.printStackTrace();
//        }
//
//    }
//        queryTask.continueWithTask(new Continuation<MetadataBuffer, Task<Void>>() {
//                                       @Override
//                                       public Task<Void> then(@NonNull Task<MetadataBuffer> task) throws Exception {
//                                           MetadataBuffer buffer = task.getResult();
//
//                                           ArrayList<DriveFile> files = new ArrayList<>();
//                                           for (Metadata m : buffer) {
//                                               Task<DriveContents> openFileTask = _driveResourceClient.openFile(m.getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);
//                                               openFileTask.addOnSuccessListener(new OnSuccessListener<DriveContents>() {
//                                                   @Override
//                                                   public void onSuccess(DriveContents driveContents) {
//                                                       try (BufferedReader reader = new BufferedReader(new InputStreamReader(driveContents.getInputStream()))) {
//                                                           StringBuilder builder = new StringBuilder();
//                                                           String line;
//                                                           while ((line = reader.readLine()) != null) {
//                                                               builder.append(line);
//                                                           }
//                                                           addToCache(builder.toString());
//                                                       } catch (Exception e) {
//                                                           Log.e(TAG, e.getMessage());
//                                                       }
//                                                   }
//                                               });
//                                           }
//                                       })
//                                   })
//    }
//
//                return _driveResourceClient.
//                        openFile(m.getDriveId()
//                                .asDriveFile(), DriveFile.MODE_READ_ONLY);
//                openFileTask.addOnSuccessListener(new OnSuccessListener<DriveContents>() {
//                    @Override
//                    public void onSuccess(DriveContents driveContents) {
//                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(driveContents.getInputStream()))) {
//                            StringBuilder builder = new StringBuilder();
//                            String line;
//                            while ((line = reader.readLine()) != null) {
//                                builder.append(line);
//                            }
//                            addToCache(builder.toString());
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                        }
//                return null;
//            }
//        }
//        new OnSuccessListener<MetadataBuffer>() {
//                    @Override
//                    public void onSuccess(MetadataBuffer metadata) {
//                        MetadataBuffer buffer = metadata;
//                        for (Metadata m : buffer) {
//                            Task<DriveContents> openFileTask = _driveResourceClient.
//                                    openFile(m.getDriveId()
//                                            .asDriveFile(), DriveFile.MODE_READ_ONLY);
//
//                            openFileTask.addOnSuccessListener(new OnSuccessListener<DriveContents>() {
//                                @Override
//                                public void onSuccess(DriveContents driveContents) {
//                                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(driveContents.getInputStream()))) {
//                                        StringBuilder builder = new StringBuilder();
//                                        String line;
//                                        while ((line = reader.readLine()) != null) {
//                                            builder.append(line);
//                                        }
//                                        addToCache(builder.toString());
//                                    } catch (Exception e) {
//                                        Log.e(TAG, e.getMessage());
//                                    }
//                                }
//                            });
//                            activity.listImported(files);
//                        }
//                    }
//                });

//            }



    /**
     * Sloooooow synchronous retrieval of files from Google Drive
     * @param extension
     * @return
     */
    public ArrayList<String> getFileList(String extension) {

        try {

            if(_account == null) {
                throw new ExecutionException("Google login failed.", new AndroidException());
            }

            Query query = new Query.Builder()
                    .addFilter(Filters.contains(SearchableField.TITLE, extension))
                    .build();

            Task<MetadataBuffer> queryTask = _driveResourceClient.query(query);
            MetadataBuffer buffer = Tasks.await(queryTask);

            for (Metadata m : buffer) {
                DriveFile file = m.getDriveId().asDriveFile();
                Task<DriveContents> openFileTask =
                        _driveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);

                DriveContents c = Tasks.await(openFileTask);
                try(BufferedReader reader = new BufferedReader(
                        new InputStreamReader(c.getInputStream()))) {
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                    files.add(builder.toString());
                } catch(Exception e) {
                    Log.e(TAG,e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch(Exception e) {
            Log.e(TAG,e.getMessage());
        }

        if(files.size() != 0) {
            return files;
        } else {
            return new ArrayList<>();
        }

    }

    public Boolean contains(final String filename) {
        //TODO
        return null;
    }


}
