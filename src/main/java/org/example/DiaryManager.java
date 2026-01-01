package org.example;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiaryManager {
    private static final String DIARY_DIR = "diary_entries";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public DiaryManager() {
        try {
            Files.createDirectories(Paths.get(DIARY_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveEntry(DiaryEntry entry) throws IOException {
        String filename = entry.getTimestamp().format(FILE_DATE_FORMAT) + "_" + entry.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".txt";
        Path path = Paths.get(DIARY_DIR, filename);
        
        // Simple text format: Title\nTimestamp\nContent
        String fileContent = entry.getTitle() + "\n" + entry.getTimestamp().toString() + "\n" + entry.getContent();
        Files.write(path, fileContent.getBytes());
        entry.setFilename(filename);
    }
    
    public void updateEntry(DiaryEntry originalEntry, DiaryEntry newEntry) throws IOException {
        deleteEntry(originalEntry);
        saveEntry(newEntry);
    }

    public void deleteEntry(DiaryEntry entry) throws IOException {
        if (entry.getFilename() != null) {
            Path path = Paths.get(DIARY_DIR, entry.getFilename());
            if (Files.exists(path)) {
                Files.delete(path);
                return;
            }
        }
        
        // Fallback: Find file by timestamp and title approximation
        List<Path> files = getAllEntryPaths();
        for (Path path : files) {
             DiaryEntry loaded = loadEntry(path);
             if (loaded != null && loaded.getTimestamp().equals(entry.getTimestamp()) && loaded.getTitle().equals(entry.getTitle())) {
                 Files.delete(path);
                 return;
             }
        }
    }

    public List<DiaryEntry> getAllEntries() {
        List<DiaryEntry> entries = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(DIARY_DIR))) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                     DiaryEntry entry = loadEntry(path);
                     if (entry != null) {
                         entries.add(entry);
                     }
                 });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Sort by timestamp descending (newest first)
        entries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));
        return entries;
    }

    private List<Path> getAllEntryPaths() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(DIARY_DIR))) {
            return paths.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }

    private DiaryEntry loadEntry(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            if (lines.size() >= 3) {
                String title = lines.get(0);
                LocalDateTime timestamp = LocalDateTime.parse(lines.get(1));
                String content = String.join("\n", lines.subList(2, lines.size()));
                DiaryEntry entry = new DiaryEntry(title, content, timestamp);
                entry.setFilename(path.getFileName().toString());
                return entry;
            }
        } catch (Exception e) {
            // Skip corrupted files
        }
        return null;
    }

    public List<DiaryEntry> searchEntries(String query) {
        String lowerQuery = query.toLowerCase();
        return getAllEntries().stream()
                .filter(e -> e.getTitle().toLowerCase().contains(lowerQuery) || e.getContent().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
}