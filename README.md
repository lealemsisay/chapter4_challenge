# Personal Diary Manager - JavaFX GUI Version

## Overview
This is a JavaFX-based GUI application for managing personal diary entries. It allows users to create, read, update, and delete diary entries with rich text formatting.

## Features
- **Main Dashboard**: Navigation pane with a list of entries.
- **Rich Text Editor**: Create and edit entries with formatting (bold, italic, etc.) using `HTMLEditor`.
- **Entry Browser**: View entries in a read-only mode using `WebView`.
- **Search**: Filter entries by title or content in real-time.
- **Theme Support**: Toggle between Light and Dark modes.
- **File Persistence**: Entries are saved as text files in a local directory.
- **Concurrency**: File operations are performed in the background to keep the UI responsive.
- **Auto-Save**: Automatically saves changes every 30 seconds while editing.
- **Progress Indicators**: Visual feedback during file operations.

## Design Choices
- **JavaFX**: Chosen for its modern UI components and rich text support.
- **File Storage**: Simple text file storage was chosen for portability and ease of implementation, as per the challenge requirements. Each entry is a separate file.
- **CompletableFuture**: Used for handling background tasks (file I/O) to prevent freezing the JavaFX Application Thread.
- **Timeline**: Used for the auto-save functionality.
- **MVC-ish Architecture**: Separated the data management (`DiaryManager`, `DiaryEntry`) from the UI (`Main`).

## How to Run
1. Ensure you have JDK 17 or later installed.
2. Open the project in an IDE (IntelliJ IDEA recommended).
3. **Important**: Run the `org.example.Launcher` class.
   - Do **not** run `org.example.Main` directly, as this may cause a "JavaFX runtime components are missing" error in some IDE configurations.
4. The application window will appear.

## Usage
- **New Entry**: Click "New Entry" to clear the editor. Enter a title and content, then click "Save".
- **Read Entry**: Select an entry from the list on the left.
- **Edit Entry**: While viewing an entry, click "Edit" to modify it.
- **Delete Entry**: Select an entry and click "Delete Entry".
- **Search**: Type in the search bar to filter the list.
- **Dark Mode**: Toggle the "Dark Mode" button to switch themes.
