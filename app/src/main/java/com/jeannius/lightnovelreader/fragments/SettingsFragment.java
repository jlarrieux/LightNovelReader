package com.jeannius.lightnovelreader.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.jeannius.lightnovelreader.DialogFragment.StringSet.BlockedStringDialogFragment;
import com.jeannius.lightnovelreader.DialogFragment.StringSet.FreeWebNovelSynonymsDialogFragment;
import com.jeannius.lightnovelreader.Interface.OnBlockedStringSetUpdatedListener;
import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymSetUpdatedListener;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.SaverLoaderUtils;
import com.jeannius.lightnovelreader.utils.BackupHelper;

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
    }
    
    private static final String FREE_WEB_NOVEL_SYNONYMS = "freeWebNovelSynonyms";
    private static final String BLOCKED_STRINGS = "blockedStrings";
    
    private Button blockedStringsButton;
    private Button freeWebNovelSynonymsButton;
    private Button exportButton;
    private Button importButton;
    
    private Set<String> freeNovelSynonyms = new HashSet<>();
    private Set<String> blockedStringsSet = new HashSet<>();
    
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        blockedStringsButton = view.findViewById(R.id.blocked_strings_button);
        freeWebNovelSynonymsButton = view.findViewById(R.id.free_web_novel_synonyms_button);
        exportButton = view.findViewById(R.id.export_button);
        importButton = view.findViewById(R.id.import_button);
        
        loadData();
        setupButtonListeners();
        
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
}