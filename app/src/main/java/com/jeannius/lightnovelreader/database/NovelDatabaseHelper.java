package com.jeannius.lightnovelreader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jeannius.lightnovelreader.model.Novel;

import java.util.ArrayList;
import java.util.List;

public class NovelDatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "novels.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table name
    private static final String TABLE_NOVELS = "novels";
    
    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_CURRENT_CHAPTER = "current_chapter";
    private static final String COLUMN_LAST_READ_DATE = "last_read_date";
    private static final String COLUMN_STATUS = "status";
    
    // Create table SQL
    private static final String CREATE_TABLE_NOVELS = "CREATE TABLE " + TABLE_NOVELS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TITLE + " TEXT NOT NULL,"
            + COLUMN_URL + " TEXT NOT NULL UNIQUE,"
            + COLUMN_CURRENT_CHAPTER + " TEXT,"
            + COLUMN_LAST_READ_DATE + " INTEGER,"
            + COLUMN_STATUS + " TEXT NOT NULL"
            + ")";
    
    public NovelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOVELS);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now, just drop and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOVELS);
        onCreate(db);
    }
    
    // Insert or update novel
    public long insertOrUpdateNovel(Novel novel) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, novel.getTitle());
        values.put(COLUMN_URL, novel.getUrl());
        values.put(COLUMN_CURRENT_CHAPTER, novel.getCurrentChapter());
        values.put(COLUMN_LAST_READ_DATE, novel.getLastReadDate());
        values.put(COLUMN_STATUS, novel.getStatus().name());
        
        // Check if novel exists
        Novel existing = getNovelByUrl(novel.getUrl());
        if (existing != null) {
            // Update
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
        
        String statusStr = cursor.getString(cursor.getColumnIndex(COLUMN_STATUS));
        novel.setStatus(Novel.Status.valueOf(statusStr));
        
        return novel;
    }
}