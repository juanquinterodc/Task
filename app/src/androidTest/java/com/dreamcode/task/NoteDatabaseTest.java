package com.dreamcode.task;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dreamcode.task.data.AppDatabase;
import com.dreamcode.task.data.Note;
import com.dreamcode.task.data.NoteDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class NoteDatabaseTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private NoteDao noteDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        noteDao = db.noteDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetNote() throws Exception {
        Note note = new Note("Test Title", "Test Content", System.currentTimeMillis(), "General", -1, false, false);
        noteDao.insert(note);
        
        List<Note> allNotes = LiveDataTestUtil.getOrAwaitValue(noteDao.getAllNotes());
        assertEquals(1, allNotes.size());
        assertEquals("Test Title", allNotes.get(0).getTitle());
    }

    @Test
    public void updateNote() throws Exception {
        Note note = new Note("Title", "Content", System.currentTimeMillis(), "General", -1, false, false);
        noteDao.insert(note);
        
        List<Note> notes = LiveDataTestUtil.getOrAwaitValue(noteDao.getAllNotes());
        Note savedNote = notes.get(0);
        savedNote.setTitle("Updated Title");
        noteDao.update(savedNote);
        
        List<Note> updatedNotes = LiveDataTestUtil.getOrAwaitValue(noteDao.getAllNotes());
        assertEquals("Updated Title", updatedNotes.get(0).getTitle());
    }

    @Test
    public void deleteNote() throws Exception {
        Note note = new Note("Title", "Content", System.currentTimeMillis(), "General", -1, false, false);
        noteDao.insert(note);
        
        List<Note> notes = LiveDataTestUtil.getOrAwaitValue(noteDao.getAllNotes());
        assertEquals(1, notes.size());
        
        noteDao.delete(notes.get(0));
        
        List<Note> notesAfterDelete = LiveDataTestUtil.getOrAwaitValue(noteDao.getAllNotes());
        assertTrue(notesAfterDelete.isEmpty());
    }
    
    @Test
    public void testSecretNotesFiltering() throws Exception {
        Note normalNote = new Note("Normal", "Content", System.currentTimeMillis(), "General", -1, false, false);
        Note secretNote = new Note("Secret", "Content", System.currentTimeMillis(), "General", -1, false, true);
        
        noteDao.insert(normalNote);
        noteDao.insert(secretNote);
        
        List<Note> allNotes = LiveDataTestUtil.getOrAwaitValue(noteDao.getAllNotes());
        assertEquals(1, allNotes.size());
        assertEquals("Normal", allNotes.get(0).getTitle());
        
        List<Note> secretNotes = LiveDataTestUtil.getOrAwaitValue(noteDao.getSecretNotes());
        assertEquals(1, secretNotes.size());
        assertEquals("Secret", secretNotes.get(0).getTitle());
    }
}
