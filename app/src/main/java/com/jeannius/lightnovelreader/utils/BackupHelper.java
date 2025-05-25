package com.jeannius.lightnovelreader.utils;

import android.content.Context;
import android.net.Uri;

import com.jeannius.lightnovelreader.database.NovelDatabaseHelper;
import com.jeannius.lightnovelreader.model.Novel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupHelper {
    
    private static final String JSON_VERSION = "version";
    private static final String JSON_BACKUP_DATE = "backupDate";
    private static final String JSON_NOVELS = "novels";
    
    private static final String JSON_NOVEL_TITLE = "title";
    private static final String JSON_NOVEL_URL = "url";
    private static final String JSON_NOVEL_CHAPTER = "currentChapter";
    private static final String JSON_NOVEL_LAST_READ = "lastReadDate";
    private static final String JSON_NOVEL_DATE_ADDED = "dateAdded";
    private static final String JSON_NOVEL_STATUS = "status";
    private static final String JSON_NOVEL_NOTES = "personalNotes";
    
    private static final int BACKUP_VERSION = 2;
    
    /**
     * Export novels to JSON
     */
    public static void exportToJson(Context context, Uri uri) throws IOException, JSONException {
        NovelDatabaseHelper dbHelper = new NovelDatabaseHelper(context);
        List<Novel> novels = dbHelper.getAllNovels();
        dbHelper.close();
        
        JSONObject backup = new JSONObject();
        backup.put(JSON_VERSION, BACKUP_VERSION);
        backup.put(JSON_BACKUP_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        
        JSONArray novelsArray = new JSONArray();
        for (Novel novel : novels) {
            JSONObject novelJson = new JSONObject();
            novelJson.put(JSON_NOVEL_TITLE, novel.getTitle());
            novelJson.put(JSON_NOVEL_URL, novel.getUrl());
            novelJson.put(JSON_NOVEL_CHAPTER, novel.getCurrentChapter());
            novelJson.put(JSON_NOVEL_LAST_READ, novel.getLastReadDate());
            novelJson.put(JSON_NOVEL_DATE_ADDED, novel.getDateAdded());
            novelJson.put(JSON_NOVEL_STATUS, novel.getStatus().name());
            novelJson.put(JSON_NOVEL_NOTES, novel.getPersonalNotes());
            novelsArray.put(novelJson);
        }
        
        backup.put(JSON_NOVELS, novelsArray);
        
        // Write to file
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(backup.toString(2).getBytes(StandardCharsets.UTF_8));
            }
        }
    }
    
    /**
     * Import novels from JSON
     */
    public static int importFromJson(Context context, Uri uri) throws IOException, JSONException {
        StringBuilder jsonString = new StringBuilder();
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        }
        
        JSONObject backup = new JSONObject(jsonString.toString());
        
        // Check version compatibility
        int version = backup.optInt(JSON_VERSION, 0);
        if (version > BACKUP_VERSION) {
            throw new JSONException("Backup version " + version + " is not supported");
        }
        
        JSONArray novelsArray = backup.getJSONArray(JSON_NOVELS);
        NovelDatabaseHelper dbHelper = new NovelDatabaseHelper(context);
        
        int importedCount = 0;
        for (int i = 0; i < novelsArray.length(); i++) {
            JSONObject novelJson = novelsArray.getJSONObject(i);
            
            Novel novel = new Novel();
            novel.setTitle(novelJson.getString(JSON_NOVEL_TITLE));
            novel.setUrl(novelJson.getString(JSON_NOVEL_URL));
            novel.setCurrentChapter(novelJson.optString(JSON_NOVEL_CHAPTER, null));
            novel.setLastReadDate(novelJson.getLong(JSON_NOVEL_LAST_READ));
            novel.setDateAdded(novelJson.optLong(JSON_NOVEL_DATE_ADDED, novel.getLastReadDate()));
            
            String statusStr = novelJson.getString(JSON_NOVEL_STATUS);
            try {
                novel.setStatus(Novel.Status.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                novel.setStatus(Novel.Status.READING); // Default if status is invalid
            }
            
            // Handle personal notes (added in version 2)
            if (version >= 2) {
                novel.setPersonalNotes(novelJson.optString(JSON_NOVEL_NOTES, ""));
            }
            
            dbHelper.insertOrUpdateNovel(novel);
            importedCount++;
        }
        
        dbHelper.close();
        return importedCount;
    }
}