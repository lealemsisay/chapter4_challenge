package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.example.DiaryEntry;
import org.example.DiaryManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    @FXML private BorderPane root;
    @FXML private TextField searchField;
    @FXML private ListView<DiaryEntry> entryListView;
    @FXML private ToggleButton themeToggle;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TextField titleField;
    @FXML private HTMLEditor contentEditor;
    @FXML private WebView entryPreview;
    @FXML private Label statusLabel;

    private DiaryManager diaryManager;
    private ObservableList<DiaryEntry> entryList;
    private boolean isDarkMode = false;
    private DiaryEntry currentEntry = null;
    private Timeline autoSaveTimeline;
    private String lastSavedContent = "";
    private String lastSavedTitle = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diaryManager = new DiaryManager();
        entryList = FXCollections.observableArrayList();
        entryListView.setItems(entryList);

        entryListView.setCellFactory(param -> new ListCell<DiaryEntry>() {
            @Override
            protected void updateItem(DiaryEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        entryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadEntryForReading(newVal);
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterEntries(newValue));

        // Initial Load
        refreshEntryList();
        prepareNewEntry();

        // Auto-save setup (every 30 seconds)
        autoSaveTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> performAutoSave()));
        autoSaveTimeline.setCycleCount(Timeline.INDEFINITE);
        autoSaveTimeline.play();
    }

    private void showProgress(boolean show) {
        progressIndicator.setVisible(show);
    }

    private void refreshEntryList() {
        showProgress(true);
        CompletableFuture.supplyAsync(() -> diaryManager.getAllEntries())
                .thenAccept(entries -> Platform.runLater(() -> {
                    entryList.setAll(entries);
                    showProgress(false);
                }));
    }

    private void filterEntries(String query) {
        showProgress(true);
        if (query == null || query.isEmpty()) {
            refreshEntryList();
        } else {
            CompletableFuture.supplyAsync(() -> diaryManager.searchEntries(query))
                    .thenAccept(entries -> Platform.runLater(() -> {
                        entryList.setAll(entries);
                        showProgress(false);
                    }));
        }
    }

    @FXML
    private void prepareNewEntry() {
        currentEntry = null;
        titleField.clear();
        contentEditor.setHtmlText("");
        titleField.setEditable(true);
        contentEditor.setVisible(true);
        entryPreview.setVisible(false);
        entryListView.getSelectionModel().clearSelection();
        lastSavedContent = "";
        lastSavedTitle = "";
        statusLabel.setText("New Entry");
    }

    private void loadEntryForReading(DiaryEntry entry) {
        currentEntry = entry;
        titleField.setText(entry.getTitle());
        titleField.setEditable(false);
        
        contentEditor.setVisible(false);
        entryPreview.setVisible(true);
        entryPreview.getEngine().loadContent(entry.getContent());
        
        lastSavedContent = entry.getContent();
        lastSavedTitle = entry.getTitle();
        statusLabel.setText("Reading: " + entry.getTitle());
    }

    @FXML
    private void loadEntryForEditing() {
        if (currentEntry != null) {
            titleField.setText(currentEntry.getTitle());
            titleField.setEditable(true);
            contentEditor.setHtmlText(currentEntry.getContent());
            
            contentEditor.setVisible(true);
            entryPreview.setVisible(false);
            
            lastSavedContent = currentEntry.getContent();
            lastSavedTitle = currentEntry.getTitle();
            statusLabel.setText("Editing: " + currentEntry.getTitle());
        }
    }
    
    // Overload for internal use if needed, though the button calls the parameterless one
    private void loadEntryForEditing(DiaryEntry entry) {
        currentEntry = entry;
        loadEntryForEditing();
    }

    private void performAutoSave() {
        if (contentEditor.isVisible()) {
            String currentContent = contentEditor.getHtmlText();
            String currentTitle = titleField.getText();

            if (!currentContent.equals(lastSavedContent) || !currentTitle.equals(lastSavedTitle)) {
                if (!currentTitle.isEmpty()) {
                    Platform.runLater(() -> statusLabel.setText("Auto-saving..."));
                    saveEntry(false);
                }
            }
        }
    }

    @FXML
    private void saveEntry() {
        saveEntry(true);
    }

    private void saveEntry(boolean showSuccessAlert) {
        String title = titleField.getText();
        String content = contentEditor.getHtmlText();

        if (title.isEmpty()) {
            if (showSuccessAlert) showAlert("Error", "Title cannot be empty.");
            return;
        }

        showProgress(true);
        CompletableFuture.runAsync(() -> {
            try {
                if (currentEntry == null) {
                    DiaryEntry newEntry = new DiaryEntry(title, content);
                    diaryManager.saveEntry(newEntry);
                    currentEntry = newEntry; 
                } else {
                    DiaryEntry updatedEntry = new DiaryEntry(title, content, currentEntry.getTimestamp());
                    updatedEntry.setFilename(currentEntry.getFilename());
                    diaryManager.updateEntry(currentEntry, updatedEntry);
                    currentEntry = updatedEntry;
                }
                
                lastSavedTitle = title;
                lastSavedContent = content;

                Platform.runLater(() -> {
                    refreshEntryList();
                    statusLabel.setText("Saved: " + title);
                    showProgress(false);
                    if (showSuccessAlert) showAlert("Success", "Entry saved successfully.");
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showProgress(false);
                    showAlert("Error", "Failed to save entry: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    private void deleteSelectedEntry() {
        DiaryEntry selected = entryListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProgress(true);
            CompletableFuture.runAsync(() -> {
                try {
                    diaryManager.deleteEntry(selected);
                    Platform.runLater(() -> {
                        refreshEntryList();
                        prepareNewEntry();
                        showProgress(false);
                        statusLabel.setText("Entry deleted");
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showProgress(false);
                        showAlert("Error", "Failed to delete entry: " + e.getMessage());
                    });
                }
            });
        }
    }

    @FXML
    private void toggleTheme() {
        isDarkMode = themeToggle.isSelected();
        if (isDarkMode) {
            root.getStyleClass().add("dark-mode");
        } else {
            root.getStyleClass().remove("dark-mode");
        }
        applyDarkThemeToEditor();
    }

    private void applyDarkThemeToEditor() {
        WebView webView = (WebView) contentEditor.lookup(".web-view");
        if (webView != null) {
            if (isDarkMode) {
                String css = getClass().getResource("/style/dark-editor.css").toExternalForm();
                webView.getEngine().setUserStyleSheetLocation(css);
            } else {
                webView.getEngine().setUserStyleSheetLocation(null);
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}