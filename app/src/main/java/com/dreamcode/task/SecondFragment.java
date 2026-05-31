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

        binding.buttonSave.setOnClickListener(v -> {
            String title = binding.editTextTitle.getText().toString();
            String content = binding.editTextContent.getText().toString();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(getContext(), R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
                return;
            }

            Note note = new Note(title, content);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase.getDatabase(getContext()).noteDao().insert(note);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), R.string.note_saved_message, Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(SecondFragment.this).popBackStack();
                    });
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
