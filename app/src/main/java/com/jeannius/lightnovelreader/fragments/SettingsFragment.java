package com.jeannius.lightnovelreader.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.jeannius.lightnovelreader.DialogFragment.StringSet.BlockedStringDialogFragment;
import com.jeannius.lightnovelreader.DialogFragment.StringSet.FreeWebNovelSynonymsDialogFragment;
import com.jeannius.lightnovelreader.Interface.OnBlockedStringSetUpdatedListener;
import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymSetUpdatedListener;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.SaverLoaderUtils;
import com.jeannius.lightnovelreader.database.NovelDatabaseHelper;
import com.jeannius.lightnovelreader.utils.BackupHelper;
import com.jeannius.lightnovelreader.utils.CloudBackupManager;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SettingsFragment extends Fragment implements OnBlockedStringSetUpdatedListener {
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup export launcher
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            exportLibrary(uri);
                        }
                    }
                }
        );
        
        // Setup import launcher
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            importLibrary(uri);
                        }
                    }
                }
        );
        
        // Setup Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
        );
    }
    
    private static final String FREE_WEB_NOVEL_SYNONYMS = "freeWebNovelSynonyms";
    private static final String BLOCKED_STRINGS = "blockedStrings";
    
    private Button blockedStringsButton;
    private Button freeWebNovelSynonymsButton;
    private Button exportButton;
    private Button importButton;
    private Button backupToCloudButton;
    private Button restoreFromCloudButton;
    private Button signInGoogleButton;
    private TextView cloudBackupStatus;
    
    private Set<String> freeNovelSynonyms = new HashSet<>();
    private Set<String> blockedStringsSet = new HashSet<>();
    
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    
    private CloudBackupManager cloudBackupManager;
    private NovelDatabaseHelper databaseHelper;
    private boolean isSignedIn = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        blockedStringsButton = view.findViewById(R.id.blocked_strings_button);
        freeWebNovelSynonymsButton = view.findViewById(R.id.free_web_novel_synonyms_button);
        exportButton = view.findViewById(R.id.export_button);
        importButton = view.findViewById(R.id.import_button);
        backupToCloudButton = view.findViewById(R.id.backup_to_cloud_button);
        restoreFromCloudButton = view.findViewById(R.id.restore_from_cloud_button);
        signInGoogleButton = view.findViewById(R.id.sign_in_google_button);
        cloudBackupStatus = view.findViewById(R.id.cloud_backup_status);
        
        // Initialize cloud backup manager and database helper
        cloudBackupManager = new CloudBackupManager(requireContext());
        databaseHelper = new NovelDatabaseHelper(requireContext());
        
        loadData();
        setupButtonListeners();
        checkGoogleSignInStatus();
        
        return view;
    }
    
    private void loadData() {
        freeNovelSynonyms = SaverLoaderUtils.loadSetFromLocal(FREE_WEB_NOVEL_SYNONYMS, getContext());
        blockedStringsSet = SaverLoaderUtils.loadSetFromLocal(BLOCKED_STRINGS, getContext());
    }
    
    private void setupButtonListeners() {
        blockedStringsButton.setOnClickListener(v -> {
            showBlockedStrings();
        });
        
        freeWebNovelSynonymsButton.setOnClickListener(v -> {
            showFreeWebNovelSynonyms();
        });
        
        exportButton.setOnClickListener(v -> {
            launchExportPicker();
        });
        
        importButton.setOnClickListener(v -> {
            showImportConfirmation();
        });
        
        signInGoogleButton.setOnClickListener(v -> {
            if (isSignedIn) {
                signOutFromGoogle();
            } else {
                signInToGoogle();
            }
        });
        
        backupToCloudButton.setOnClickListener(v -> {
            backupToCloud();
        });
        
        restoreFromCloudButton.setOnClickListener(v -> {
            showRestoreConfirmation();
        });
    }
    
    private void showBlockedStrings() {
        DialogFragment dialogFragment = new BlockedStringDialogFragment(BLOCKED_STRINGS, this, "Blocked Strings");
        dialogFragment.show(getParentFragmentManager(), "BlockedStrings");
    }
    
    private void showFreeWebNovelSynonyms() {
        DialogFragment dialogFragment = new FreeWebNovelSynonymsDialogFragment(FREE_WEB_NOVEL_SYNONYMS, this, "FreeWebNovel Synonyms");
        dialogFragment.show(getParentFragmentManager(), "FreeWebNovelSynonyms");
    }
    
    @Override
    public void reloadSynonyms() {
        freeNovelSynonyms = SaverLoaderUtils.loadSetFromLocal(FREE_WEB_NOVEL_SYNONYMS, getContext());
    }
    
    @Override
    public void reloadBlockedStrings() {
        blockedStringsSet = SaverLoaderUtils.loadSetFromLocal(BLOCKED_STRINGS, getContext());
    }
    
    private void launchExportPicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        
        String fileName = "novel_library_backup_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + 
                ".json";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        
        exportLauncher.launch(intent);
    }
    
    private void showImportConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Import Library")
                .setMessage("This will merge imported novels with your existing library. Continue?")
                .setPositiveButton("Import", (dialog, which) -> launchImportPicker())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void launchImportPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        
        importLauncher.launch(intent);
    }
    
    private void exportLibrary(Uri uri) {
        try {
            BackupHelper.exportToJson(requireContext(), uri);
            Toast.makeText(requireContext(), "Library exported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void importLibrary(Uri uri) {
        try {
            int count = BackupHelper.importFromJson(requireContext(), uri);
            Toast.makeText(requireContext(), 
                    "Imported " + count + " novel" + (count != 1 ? "s" : ""), 
                    Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            Toast.makeText(requireContext(), "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    // Cloud backup methods
    private void checkGoogleSignInStatus() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            isSignedIn = true;
            cloudBackupManager.initializeDriveService(account);
            updateUIForSignedInState();
            checkCloudBackupStatus();
        } else {
            isSignedIn = false;
            updateUIForSignedOutState();
        }
    }
    
    private void updateUIForSignedInState() {
        signInGoogleButton.setText("Sign out from Google Drive");
        backupToCloudButton.setEnabled(true);
        restoreFromCloudButton.setEnabled(true);
        cloudBackupStatus.setText("Signed in to Google Drive");
    }
    
    private void updateUIForSignedOutState() {
        signInGoogleButton.setText("Sign in to Google Drive");
        backupToCloudButton.setEnabled(false);
        restoreFromCloudButton.setEnabled(false);
        cloudBackupStatus.setText("Not signed in to Google Drive");
    }
    
    private void signInToGoogle() {
        Intent signInIntent = cloudBackupManager.getSignInClient().getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
    
    private void signOutFromGoogle() {
        cloudBackupManager.getSignInClient().signOut()
                .addOnCompleteListener(requireActivity(), task -> {
                    isSignedIn = false;
                    updateUIForSignedOutState();
                    Toast.makeText(requireContext(), "Signed out from Google Drive", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            isSignedIn = true;
            cloudBackupManager.initializeDriveService(account);
            updateUIForSignedInState();
            checkCloudBackupStatus();
            Toast.makeText(requireContext(), "Signed in successfully", Toast.LENGTH_SHORT).show();
        } catch (ApiException e) {
            isSignedIn = false;
            updateUIForSignedOutState();
            Toast.makeText(requireContext(), "Sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void checkCloudBackupStatus() {
        databaseHelper.checkCloudBackupExists(cloudBackupManager, (exists, lastModified) -> {
            requireActivity().runOnUiThread(() -> {
                if (exists && lastModified != null) {
                    cloudBackupStatus.setText("Last backup: " + formatBackupTime(lastModified));
                } else {
                    cloudBackupStatus.setText("Signed in to Google Drive (No backup found)");
                }
            });
        });
    }
    
    private String formatBackupTime(String isoTime) {
        try {
            // Parse ISO time and format it nicely
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            Date date = isoFormat.parse(isoTime);
            return displayFormat.format(date);
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private void backupToCloud() {
        // Check if Google Sign-In account is available
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account == null) {
            Toast.makeText(requireContext(), "Please sign in to Google first", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Check if the account has a valid Android account
        if (account.getAccount() == null) {
            Log.e("SettingsFragment", "GoogleSignInAccount details - Email: " + account.getEmail() +
                  ", DisplayName: " + account.getDisplayName() + 
                  ", Account: " + account.getAccount());
            Toast.makeText(requireContext(), "Google account data is incomplete. Check Google Cloud Console OAuth configuration.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Ensure drive service is initialized
        cloudBackupManager.initializeDriveService(account);
        
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Backing up to Google Drive");
        progressDialog.setMessage("Preparing backup...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        databaseHelper.backupToCloud(cloudBackupManager, new CloudBackupManager.BackupCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Backup completed successfully", Toast.LENGTH_SHORT).show();
                    checkCloudBackupStatus(); // Refresh backup status
                });
            }
            
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Backup failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onProgress(String message) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.setMessage(message);
                });
            }
        });
    }
    
    private void showRestoreConfirmation() {
        // Check if Google Sign-In account is available
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account == null || account.getAccount() == null) {
            Toast.makeText(requireContext(), "Google account not available. Please sign out and sign in again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Ensure drive service is initialized
        cloudBackupManager.initializeDriveService(account);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Restore from Google Drive")
                .setMessage("This will replace your current library with the backup from Google Drive. Your current data will be backed up locally first. Continue?")
                .setPositiveButton("Restore", (dialog, which) -> restoreFromCloud())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void restoreFromCloud() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Restoring from Google Drive");
        progressDialog.setMessage("Downloading backup...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        databaseHelper.restoreFromCloud(cloudBackupManager, new CloudBackupManager.BackupCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Restore completed successfully", Toast.LENGTH_SHORT).show();
                    // Optionally refresh the UI or notify other fragments
                });
            }
            
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Restore failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onProgress(String message) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.setMessage(message);
                });
            }
        });
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cloudBackupManager != null) {
            cloudBackupManager.shutdown();
        }
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}