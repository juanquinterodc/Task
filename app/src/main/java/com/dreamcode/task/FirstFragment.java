package com.dreamcode.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dreamcode.task.databinding.FragmentFirstBinding;
import com.dreamcode.task.data.AppDatabase;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

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

        AppDatabase db = AppDatabase.getDatabase(getContext());
        NoteAdapter adapter = new NoteAdapter(note -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.noteDao().delete(note);
            });
        });

        binding.recyclerViewNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNotes.setAdapter(adapter);

        db.noteDao().getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
