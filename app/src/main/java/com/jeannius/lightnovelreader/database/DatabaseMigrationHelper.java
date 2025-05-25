package com.jeannius.lightnovelreader.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.jeannius.lightnovelreader.SaverLoaderUtils;
import com.jeannius.lightnovelreader.model.Novel;

import java.util.HashMap;
import java.util.Map;

public class DatabaseMigrationHelper {
    
    private static final String PREFS_NAME = "NovelReaderPrefs";
    private static final String KEY_MIGRATION_DONE = "database_migration_done";
    private static final String NOVEL_MAP_FILE_NAME = "novelMapFileName";
    
    /**
     * Migrate from HashMap file storage to SQLite database
     */
    public static void migrateToDatabase(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean migrationDone = prefs.getBoolean(KEY_MIGRATION_DONE, false);
        
        if (!migrationDone) {
            // Load existing novel map
            HashMap<String, String> novelMap = SaverLoaderUtils.loadNovelMapFromLocal(NOVEL_MAP_FILE_NAME, context);
            
            if (!novelMap.isEmpty()) {
                NovelDatabaseHelper dbHelper = new NovelDatabaseHelper(context);
                
                // Migrate each novel
                for (Map.Entry<String, String> entry : novelMap.entrySet()) {
                    String title = entry.getKey();
                    String url = entry.getValue();
                    
                    Novel novel = new Novel(title, url);
                    novel.setStatus(Novel.Status.READING); // Default status for existing novels
                    novel.setLastReadDate(System.currentTimeMillis());
                    
                    dbHelper.insertOrUpdateNovel(novel);
                }
                
                dbHelper.close();
            }
            
            // Mark migration as done
            prefs.edit().putBoolean(KEY_MIGRATION_DONE, true).apply();
        }
    }
    
    /**
     * Check if migration has been done
     */
    public static boolean isMigrationDone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_MIGRATION_DONE, false);
    }
}