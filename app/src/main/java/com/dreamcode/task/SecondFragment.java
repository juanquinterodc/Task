package com.dreamcode.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dreamcode.task.data.AppDatabase;
import com.dreamcode.task.data.Note;
import com.dreamcode.task.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private int noteId = -1;
    private long noteTimestamp = -1;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            noteId = getArguments().getInt("noteId", -1);
            String title = getArguments().getString("noteTitle");
            String content = getArguments().getString("noteContent");
            // We might want to preserve the original timestamp when editing
            noteTimestamp = getArguments().getLong("noteTimestamp", -1);

            if (noteId != -1) {
                binding.editTextTitle.setText(title);
                binding.editTextContent.setText(content);
                binding.buttonSave.setText("Update Note");
            }
        }

        binding.buttonSave.setOnClickListener(v -> {
            String title = binding.editTextTitle.getText().toString();
            String content = binding.editTextContent.getText().toString();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(getContext(), R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(getContext());
                long timestamp = (noteTimestamp != -1) ? noteTimestamp : System.currentTimeMillis();
                
                if (noteId == -1) {
                    db.noteDao().insert(new Note(title, content, timestamp));
                } else {
                    Note note = new Note(title, content, timestamp);
                    note.setId(noteId);
                    db.noteDao().update(note);
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), R.string.note_saved_message, Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(SecondFragment.this).popBackStack();
                    });
                }
            });
        });

        // Hide FAB on this screen
        View fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
