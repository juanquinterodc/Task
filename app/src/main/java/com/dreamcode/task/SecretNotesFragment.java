package com.dreamcode.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dreamcode.task.data.AppDatabase;
import com.dreamcode.task.data.Note;
import com.dreamcode.task.databinding.FragmentSecretNotesBinding;

import java.util.List;

public class SecretNotesFragment extends Fragment {

    private FragmentSecretNotesBinding binding;
    private NoteAdapter adapter;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecretNotesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getDatabase(getContext());
        adapter = new NoteAdapter(
                note -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.noteDao().delete(note);
                    });
                },
                note -> {
                    Bundle bundle = createNoteBundle(note);
                    NavHostFragment.findNavController(SecretNotesFragment.this)
                            .navigate(R.id.action_SecretNotesFragment_to_SecondFragment, bundle);
                },
                note -> {
                    Bundle bundle = createNoteBundle(note);
                    NavHostFragment.findNavController(SecretNotesFragment.this)
                            .navigate(R.id.action_SecretNotesFragment_to_ViewNoteFragment, bundle);
                }
        );

        binding.recyclerViewSecretNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewSecretNotes.setAdapter(adapter);

        db.noteDao().getSecretNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
