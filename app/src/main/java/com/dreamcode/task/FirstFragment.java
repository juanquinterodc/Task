package com.dreamcode.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dreamcode.task.databinding.FragmentFirstBinding;
import com.dreamcode.task.data.AppDatabase;
import com.dreamcode.task.data.Note;

import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private NoteAdapter adapter;
    private AppDatabase db;
    private LiveData<List<Note>> currentNotesLiveData;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
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
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
                },
                note -> {
                    Bundle bundle = createNoteBundle(note);
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_ViewNoteFragment, bundle);
                }
        );
        
        binding.recyclerViewNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNotes.setAdapter(adapter);

        observeNotes(db.noteDao().getAllNotes());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchNotes(newText);
                return true;
            }
        });

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                observeNotes(db.noteDao().getAllNotes());
            } else if (checkedId == R.id.chip_filter_general) {
                observeNotes(db.noteDao().getNotesByCategory("General"));
            } else if (checkedId == R.id.chip_filter_work) {
                observeNotes(db.noteDao().getNotesByCategory("Work"));
            } else if (checkedId == R.id.chip_filter_personal) {
                observeNotes(db.noteDao().getNotesByCategory("Personal"));
            } else if (checkedId == R.id.chip_filter_ideas) {
                observeNotes(db.noteDao().getNotesByCategory("Ideas"));
            }
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

    private void searchNotes(String query) {
        if (query == null || query.isEmpty()) {
            observeNotes(db.noteDao().getAllNotes());
        } else {
            observeNotes(db.noteDao().searchNotes("%" + query + "%"));
        }
    }

    private void observeNotes(LiveData<List<Note>> notesLiveData) {
        if (currentNotesLiveData != null) {
            currentNotesLiveData.removeObservers(getViewLifecycleOwner());
        }
        currentNotesLiveData = notesLiveData;
        currentNotesLiveData.observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
