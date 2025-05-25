package com.jeannius.lightnovelreader.model;

public class Novel {
    public enum Status {
        READING("Currently Reading"),
        COMPLETED("Completed"),
        DROPPED("Dropped"),
        PLAN_TO_READ("Plan to Read");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private long id;
    private String title;
    private String url;
    private String currentChapter;
    private long lastReadDate;
    private Status status;
    
    public Novel() {
        this.status = Status.READING; // Default status
        this.lastReadDate = System.currentTimeMillis();
    }
    
    public Novel(String title, String url) {
        this();
        this.title = title;
        this.url = url;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getCurrentChapter() {
        return currentChapter;
    }
    
    public void setCurrentChapter(String currentChapter) {
        this.currentChapter = currentChapter;
    }
    
    public long getLastReadDate() {
        return lastReadDate;
    }
    
    public void setLastReadDate(long lastReadDate) {
        this.lastReadDate = lastReadDate;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getStatusDisplayName() {
        return status.getDisplayName();
    }
}