package org.example;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DiaryEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String content;
    private LocalDateTime timestamp;
    // Transient field to store the filename for file operations
    private transient String filename;

    public DiaryEntry(String title, String content) {
        this.title = title;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public DiaryEntry(String title, String content, LocalDateTime timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return title + " (" + timestamp.toLocalDate() + ")";
    }
}