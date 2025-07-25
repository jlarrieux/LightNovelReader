package com.jeannius.lightnovelreader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jeannius.lightnovelreader.model.Novel;
import com.jeannius.lightnovelreader.utils.CloudBackupManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NovelDatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "novels.db";
    private static final int DATABASE_VERSION = 3;
    
    private Context context;
    
    // Table name
    private static final String TABLE_NOVELS = "novels";
    
    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_CURRENT_CHAPTER = "current_chapter";
    private static final String COLUMN_LAST_READ_DATE = "last_read_date";
    private static final String COLUMN_DATE_ADDED = "date_added";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_PERSONAL_NOTES = "personal_notes";
    
    // Create table SQL
    private static final String CREATE_TABLE_NOVELS = "CREATE TABLE " + TABLE_NOVELS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TITLE + " TEXT NOT NULL,"
            + COLUMN_URL + " TEXT NOT NULL UNIQUE,"
            + COLUMN_CURRENT_CHAPTER + " TEXT,"
            + COLUMN_LAST_READ_DATE + " INTEGER,"
            + COLUMN_DATE_ADDED + " INTEGER,"
            + COLUMN_STATUS + " TEXT NOT NULL,"
            + COLUMN_PERSONAL_NOTES + " TEXT"
            + ")";
    
    public NovelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOVELS);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add date_added column to existing table
            db.execSQL("ALTER TABLE " + TABLE_NOVELS + " ADD COLUMN " + COLUMN_DATE_ADDED + " INTEGER DEFAULT " + System.currentTimeMillis());
        }
        if (oldVersion < 3) {
            // Add personal_notes column to existing table
            db.execSQL("ALTER TABLE " + TABLE_NOVELS + " ADD COLUMN " + COLUMN_PERSONAL_NOTES + " TEXT DEFAULT ''");
        }
    }
    
    // Insert or update novel
    public long insertOrUpdateNovel(Novel novel) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, novel.getTitle());
        values.put(COLUMN_URL, novel.getUrl());
        values.put(COLUMN_CURRENT_CHAPTER, novel.getCurrentChapter());
        values.put(COLUMN_LAST_READ_DATE, novel.getLastReadDate());
        values.put(COLUMN_DATE_ADDED, novel.getDateAdded());
        values.put(COLUMN_STATUS, novel.getStatus().name());
        values.put(COLUMN_PERSONAL_NOTES, novel.getPersonalNotes());
        
        // Check if novel exists
        Novel existing = getNovelByUrl(novel.getUrl());
        if (existing != null) {
            // Update - but preserve original date_added
            values.put(COLUMN_DATE_ADDED, existing.getDateAdded());
            return db.update(TABLE_NOVELS, values, COLUMN_URL + " = ?", 
                    new String[]{novel.getUrl()});
        } else {
            // Insert
            return db.insert(TABLE_NOVELS, null, values);
        }
    }
    
    // Get novel by URL
    public Novel getNovelByUrl(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_NOVELS, null, 
                COLUMN_URL + " = ?", new String[]{url}, 
                null, null, null);
        
        Novel novel = null;
        if (cursor.moveToFirst()) {
            novel = cursorToNovel(cursor);
        }
        cursor.close();
        
        return novel;
    }
    
    // Get all novels
    public List<Novel> getAllNovels() {
        List<Novel> novels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_NOVELS, null, null, null, null, null, 
                COLUMN_LAST_READ_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                novels.add(cursorToNovel(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return novels;
    }
    
    // Get novels by status
    public List<Novel> getNovelsByStatus(Novel.Status status) {
        List<Novel> novels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_NOVELS, null, 
                COLUMN_STATUS + " = ?", new String[]{status.name()}, 
                null, null, COLUMN_LAST_READ_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                novels.add(cursorToNovel(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return novels;
    }
    
    // Update novel status
    public int updateNovelStatus(String url, Novel.Status status) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status.name());
        
        return db.update(TABLE_NOVELS, values, COLUMN_URL + " = ?", 
                new String[]{url});
    }
    
    // Update personal notes
    public int updatePersonalNotes(String url, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_PERSONAL_NOTES, notes);
        
        return db.update(TABLE_NOVELS, values, COLUMN_URL + " = ?", 
                new String[]{url});
    }
    
    // Delete novel
    public int deleteNovel(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOVELS, COLUMN_URL + " = ?", new String[]{url});
    }
    
    // Helper method to convert cursor to Novel
    private Novel cursorToNovel(Cursor cursor) {
        Novel novel = new Novel();
        novel.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        novel.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        novel.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
        novel.setCurrentChapter(cursor.getString(cursor.getColumnIndex(COLUMN_CURRENT_CHAPTER)));
        novel.setLastReadDate(cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_READ_DATE)));
        
        // Handle date_added which might be null for existing records
        int dateAddedIndex = cursor.getColumnIndex(COLUMN_DATE_ADDED);
        if (dateAddedIndex != -1 && !cursor.isNull(dateAddedIndex)) {
            novel.setDateAdded(cursor.getLong(dateAddedIndex));
        } else {
            // Use last read date as fallback for old records
            novel.setDateAdded(novel.getLastReadDate());
        }
        
        String statusStr = cursor.getString(cursor.getColumnIndex(COLUMN_STATUS));
        novel.setStatus(Novel.Status.valueOf(statusStr));
        
        // Handle personal_notes which might be null for existing records
        int notesIndex = cursor.getColumnIndex(COLUMN_PERSONAL_NOTES);
        if (notesIndex != -1 && !cursor.isNull(notesIndex)) {
            novel.setPersonalNotes(cursor.getString(notesIndex));
        } else {
            novel.setPersonalNotes("");
        }
        
        return novel;
    }
    
    // Cloud backup methods
    
    /**
     * Backs up the database to Google Drive
     * @param cloudBackupManager The CloudBackupManager instance
     * @param callback Callback for backup progress and results
     */
    public void backupToCloud(CloudBackupManager cloudBackupManager, CloudBackupManager.BackupCallback callback) {
        // Ensure all data is written to database file
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Handle WAL checkpoint - ensure all changes are written to main database file
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Cursor cursor = db.rawQuery("PRAGMA wal_checkpoint(FULL)", null);
            if (cursor != null) {
                cursor.close();
            }
        }
        
        // Close database connections to ensure file consistency
        db.close();
        
        // Get the database file
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        
        if (!databaseFile.exists()) {
            callback.onError("Database file not found: " + databaseFile.getAbsolutePath());
            return;
        }
        
        // Perform the backup
        cloudBackupManager.backupDatabase(databaseFile, callback);
    }
    
    /**
     * Restores the database from Google Drive
     * @param cloudBackupManager The CloudBackupManager instance
     * @param callback Callback for restore progress and results
     */
    public void restoreFromCloud(CloudBackupManager cloudBackupManager, CloudBackupManager.BackupCallback callback) {
        // Close current database connections
        close();
        
        // Get the database file path
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        
        // Create backup of current database before restore
        File backupFile = new File(databaseFile.getParent(), DATABASE_NAME + ".backup");
        if (databaseFile.exists()) {
            try {
                copyFile(databaseFile, backupFile);
            } catch (Exception e) {
                callback.onError("Failed to create local backup: " + e.getMessage());
                return;
            }
        }
        
        // Restore from cloud
        cloudBackupManager.restoreDatabase(databaseFile, new CloudBackupManager.BackupCallback() {
            @Override
            public void onSuccess() {
                // Verify database integrity
                if (verifyDatabaseIntegrity()) {
                    // Clean up backup file on successful restore
                    if (backupFile.exists()) {
                        backupFile.delete();
                    }
                    callback.onSuccess();
                } else {
                    // Restore failed, revert to backup
                    try {
                        if (backupFile.exists()) {
                            copyFile(backupFile, databaseFile);
                            backupFile.delete();
                        }
                        callback.onError("Restored database is corrupted. Reverted to local backup.");
                    } catch (Exception e) {
                        callback.onError("Database corruption detected and failed to revert: " + e.getMessage());
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                // Restore local backup on error
                try {
                    if (backupFile.exists()) {
                        copyFile(backupFile, databaseFile);
                        backupFile.delete();
                    }
                } catch (Exception e) {
                    // Log but don't override original error
                }
                callback.onError(error);
            }
            
            @Override
            public void onProgress(String message) {
                callback.onProgress(message);
            }
        });
    }
    
    /**
     * Checks if a cloud backup exists
     * @param cloudBackupManager The CloudBackupManager instance
     * @param callback Callback for backup existence check
     */
    public void checkCloudBackupExists(CloudBackupManager cloudBackupManager, CloudBackupManager.BackupExistsCallback callback) {
        cloudBackupManager.checkBackupExists(callback);
    }
    
    /**
     * Verifies database integrity after restore
     * @return true if database is valid, false otherwise
     */
    private boolean verifyDatabaseIntegrity() {
        try {
            SQLiteDatabase testDb = SQLiteDatabase.openDatabase(
                context.getDatabasePath(DATABASE_NAME).getAbsolutePath(),
                null,
                SQLiteDatabase.OPEN_READONLY
            );
            
            // Try to perform a simple query
            Cursor cursor = testDb.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOVELS, null);
            boolean isValid = cursor.moveToFirst();
            cursor.close();
            testDb.close();
            
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Copies a file from source to destination
     * @param source Source file
     * @param destination Destination file
     * @throws Exception if copy fails
     */
    private void copyFile(File source, File destination) throws Exception {
        java.io.FileInputStream inputStream = new java.io.FileInputStream(source);
        java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destination);
        
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        inputStream.close();
        outputStream.close();
    }
}