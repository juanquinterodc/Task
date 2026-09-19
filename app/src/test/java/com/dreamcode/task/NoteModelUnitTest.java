package com.dreamcode.task;

import com.dreamcode.task.data.Note;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoteModelUnitTest {

    @Test
    public void noteCreationAndGetters() {
        long now = System.currentTimeMillis();
        Note note = new Note("Shopping", "Milk, Bread", now, "Personal", 123456L, true, false);

        assertEquals("Shopping", note.getTitle());
        assertEquals("Milk, Bread", note.getContent());
        assertEquals(now, note.getTimestamp());
        assertEquals("Personal", note.getCategory());
        assertEquals(123456L, note.getReminderTime());
        assertTrue(note.isChecklist());
        assertFalse(note.isSecret());
    }

    @Test
    public void noteSetters() {
        Note note = new Note("Initial", "Content", 1000L, "General", -1L, false, false);
        note.setId(42);
        note.setTitle("Modified");
        note.setContent("Updated content");
        note.setCategory("Work");
        note.setReminderTime(2000L);
        note.setChecklist(true);
        note.setSecret(true);

        assertEquals(42, note.getId());
        assertEquals("Modified", note.getTitle());
        assertEquals("Updated content", note.getContent());
        assertEquals("Work", note.getCategory());
        assertEquals(2000L, note.getReminderTime());
        assertTrue(note.isChecklist());
        assertTrue(note.isSecret());
    }

    @Test
    public void notePreviewFormattingLogic() {
        // Test preview stripping logic mirroring NoteAdapter
        String rawContent = "☐ Buy milk\n☑ Buy bread";
        String cleaned = rawContent.replace("☐ ", "").replace("☑ ", "").trim();
        assertEquals("Buy milk\nBuy bread", cleaned);
    }
}
