package com.dreamcode.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dreamcode.task.data.AppDatabase;
import com.dreamcode.task.data.Note;
import com.dreamcode.task.databinding.FragmentCalendarBinding;

import java.util.Calendar;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private NoteAdapter adapter;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getDatabase(getContext());
        adapter = new NoteAdapter(
                note -> AppDatabase.databaseWriteExecutor.execute(() -> db.noteDao().delete(note)),
                note -> {
                    Bundle bundle = createNoteBundle(note);
                    NavHostFragment.findNavController(CalendarFragment.this)
                            .navigate(R.id.action_CalendarFragment_to_SecondFragment, bundle);
                },
                note -> {
                    Bundle bundle = createNoteBundle(note);
                    NavHostFragment.findNavController(CalendarFragment.this)
                            .navigate(R.id.action_CalendarFragment_to_ViewNoteFragment, bundle);
                }
        );

        binding.recyclerViewCalendarNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCalendarNotes.setAdapter(adapter);

        binding.calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            updateNotesForDate(year, month, dayOfMonth);
        });

        // Initialize with today's date
        Calendar today = Calendar.getInstance();
        updateNotesForDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
    }

    private Bundle createNoteBundle(Note note) {
        Bundle bundle = new Bundle();
        bundle.putInt("noteId", note.getId());
        bundle.putString("noteTitle", note.getTitle());
        bundle.putString("noteContent", note.getContent());
        bundle.putString("noteCategory", note.getCategory());
        bundle.putLong("noteTimestamp", note.getTimestamp());
        bundle.putLong("noteReminderTime", note.getReminderTime());
        bundle.putBoolean("noteIsChecklist", note.isChecklist());
        bundle.putBoolean("noteIsSecret", note.isSecret());
        return bundle;
    }

    private void updateNotesForDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        long startOfDay = calendar.getTimeInMillis();
        calendar.set(year, month, dayOfMonth, 23, 59, 59);
        long endOfDay = calendar.getTimeInMillis();

        db.noteDao().getNotesByDate(startOfDay, endOfDay).observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
            if (notes.isEmpty()) {
                binding.textViewNoNotes.setVisibility(View.VISIBLE);
                binding.recyclerViewCalendarNotes.setVisibility(View.GONE);
            } else {
                binding.textViewNoNotes.setVisibility(View.GONE);
                binding.recyclerViewCalendarNotes.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
