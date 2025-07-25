package com.jeannius.lightnovelreader.utils;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.FileContent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudBackupManager {
    private static final String TAG = "CloudBackupManager";
    private static final String BACKUP_FILENAME = "lightnovel_backup.db";
    private static final String APP_FOLDER = "appDataFolder";
    
    private Context context;
    private Drive driveService;
    private GoogleSignInClient googleSignInClient;
    private ExecutorService executor;
    
    public interface BackupCallback {
        void onSuccess();
        void onError(String error);
        void onProgress(String message);
    }
    
    public CloudBackupManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        if (isGooglePlayServicesAvailable()) {
            initializeGoogleSignIn();
        }
    }
    
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services not available. Result code: " + resultCode);
            return false;
        }
        return true;
    }
    
    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }
    
    private void executeWithRetry(Runnable operation, BackupCallback callback, int maxRetries) {
        new Thread(() -> {
            int attempts = 0;
            while (attempts < maxRetries) {
                try {
                    operation.run();
                    return;
                } catch (Exception e) {
                    attempts++;
                    if (attempts >= maxRetries) {
                        callback.onError("Operation failed after " + maxRetries + " attempts: " + e.getMessage());
                        return;
                    }
                    
                    try {
                        Thread.sleep(2000 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        callback.onError("Operation interrupted");
                        return;
                    }
                }
            }
        }).start();
    }
    
    public GoogleSignInClient getSignInClient() {
        if (googleSignInClient == null) {
            Log.w(TAG, "GoogleSignInClient is null. Google Play Services may not be available.");
        }
        return googleSignInClient;
    }
    
    public boolean isConfigured() {
        return googleSignInClient != null && isGooglePlayServicesAvailable();
    }
    
    public void initializeDriveService(GoogleSignInAccount account) {
        if (account == null) {
            Log.w(TAG, "Cannot initialize drive service: GoogleSignInAccount is null");
            return;
        }
        
        android.accounts.Account androidAccount = account.getAccount();
        if (androidAccount == null) {
            Log.w(TAG, "Cannot initialize drive service: Android account is null");
            return;
        }
        
        if (androidAccount.name == null || androidAccount.name.trim().isEmpty()) {
            Log.w(TAG, "Cannot initialize drive service: Account name is null or empty");
            return;
        }
        
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singletonList(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(androidAccount);
        
        driveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Light Novel Reader")
                .build();
        
        Log.d(TAG, "Drive service initialized successfully for account: " + androidAccount.name);
    }
    
    public void backupDatabase(java.io.File databaseFile, BackupCallback callback) {
        if (!isConfigured()) {
            callback.onError("Google services not properly configured. Please check Google Play Services and app configuration.");
            return;
        }
        
        if (driveService == null) {
            callback.onError("Drive service not initialized. Please sign out and sign in again to Google.");
            return;
        }
        
        executor.execute(() -> {
            try {
                callback.onProgress("Searching for existing backup...");
                
                // Search for existing backup file in app folder
                FileList result = driveService.files().list()
                        .setSpaces(APP_FOLDER)
                        .setQ("name='" + BACKUP_FILENAME + "'")
                        .execute();
                
                List<File> files = result.getFiles();
                
                FileContent mediaContent = new FileContent("application/x-sqlite3", databaseFile);
                
                File uploadedFile;
                if (files != null && !files.isEmpty()) {
                    // Update existing file - don't set parents for updates
                    callback.onProgress("Updating existing backup...");
                    String fileId = files.get(0).getId();
                    
                    File updateMetadata = new File();
                    updateMetadata.setName(BACKUP_FILENAME);
                    // Note: Don't set parents for update operations
                    
                    uploadedFile = driveService.files().update(fileId, updateMetadata, mediaContent).execute();
                    Log.d(TAG, "Updated existing backup file: " + uploadedFile.getId());
                } else {
                    // Create new file - parents can only be set during creation
                    callback.onProgress("Creating new backup...");
                    
                    File createMetadata = new File();
                    createMetadata.setName(BACKUP_FILENAME);
                    createMetadata.setParents(Collections.singletonList(APP_FOLDER));
                    
                    uploadedFile = driveService.files().create(createMetadata, mediaContent).execute();
                    Log.d(TAG, "Created new backup file: " + uploadedFile.getId());
                }
                
                callback.onSuccess();
                
            } catch (IOException e) {
                Log.e(TAG, "Error backing up database", e);
                callback.onError("Backup failed: " + e.getMessage());
            }
        });
    }
    
    public void restoreDatabase(java.io.File destinationFile, BackupCallback callback) {
        if (!isConfigured()) {
            callback.onError("Google services not properly configured. Please check Google Play Services and app configuration.");
            return;
        }
        
        if (driveService == null) {
            callback.onError("Drive service not initialized. Please sign out and sign in again to Google.");
            return;
        }
        
        executor.execute(() -> {
            try {
                callback.onProgress("Searching for backup file...");
                
                // Search for backup file in app folder
                FileList result = driveService.files().list()
                        .setSpaces(APP_FOLDER)
                        .setQ("name='" + BACKUP_FILENAME + "'")
                        .execute();
                
                List<File> files = result.getFiles();
                if (files == null || files.isEmpty()) {
                    callback.onError("No backup file found in Google Drive");
                    return;
                }
                
                callback.onProgress("Downloading backup...");
                String fileId = files.get(0).getId();
                
                // Download file content
                java.io.InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream();
                
                // Write to destination file
                java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destinationFile);
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                inputStream.close();
                outputStream.close();
                
                Log.d(TAG, "Database restored successfully");
                callback.onSuccess();
                
            } catch (IOException e) {
                Log.e(TAG, "Error restoring database", e);
                callback.onError("Restore failed: " + e.getMessage());
            }
        });
    }
    
    public void checkBackupExists(BackupExistsCallback callback) {
        if (driveService == null) {
            callback.onResult(false, null);
            return;
        }
        
        executor.execute(() -> {
            try {
                FileList result = driveService.files().list()
                        .setSpaces(APP_FOLDER)
                        .setQ("name='" + BACKUP_FILENAME + "'")
                        .setFields("files(id,name,modifiedTime)")
                        .execute();
                
                List<File> files = result.getFiles();
                if (files != null && !files.isEmpty()) {
                    File backupFile = files.get(0);
                    callback.onResult(true, backupFile.getModifiedTime().toString());
                } else {
                    callback.onResult(false, null);
                }
                
            } catch (IOException e) {
                Log.e(TAG, "Error checking backup existence", e);
                callback.onResult(false, null);
            }
        });
    }
    
    public interface BackupExistsCallback {
        void onResult(boolean exists, String lastModified);
    }
    
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}