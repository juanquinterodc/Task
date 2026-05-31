package com.dreamcode.task.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;
    private long timestamp;
    private String category;
    private long reminderTime;
    private boolean isChecklist;
    private boolean isSecret;

    public Note(String title, String content, long timestamp, String category, long reminderTime, boolean isChecklist, boolean isSecret) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.category = category;
        this.reminderTime = reminderTime;
        this.isChecklist = isChecklist;
        this.isSecret = isSecret;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isChecklist() {
        return isChecklist;
    }

    public void setChecklist(boolean checklist) {
        isChecklist = checklist;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }
}
