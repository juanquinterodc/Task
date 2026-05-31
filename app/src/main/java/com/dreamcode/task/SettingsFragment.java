package com.dreamcode.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dreamcode.task.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private static final String PREFS_NAME = "DreamCodeNotesPrefs";
    private static final String KEY_VAULT_PIN = "vault_pin";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentPin = prefs.getString(KEY_VAULT_PIN, null);

        updatePinStatus(currentPin);

        binding.buttonSavePin.setOnClickListener(v -> {
            String pin = binding.editTextPin.getText().toString();
            if (pin.length() == 4) {
                prefs.edit().putString(KEY_VAULT_PIN, pin).apply();
                updatePinStatus(pin);
                Toast.makeText(getContext(), "PIN saved successfully", Toast.LENGTH_SHORT).show();
                binding.editTextPin.setText("");
            } else {
                Toast.makeText(getContext(), "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePinStatus(String pin) {
        if (pin != null) {
            binding.textViewPinStatus.setText("PIN is set");
            binding.textViewPinStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else {
            binding.textViewPinStatus.setText("PIN is not set");
            binding.textViewPinStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
