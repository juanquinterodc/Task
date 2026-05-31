package com.dreamcode.task;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private int noteId = -1;
    private long noteTimestamp = -1;
    private long reminderTime = -1;
    private final Calendar reminderCalendar = Calendar.getInstance();

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
            String category = getArguments().getString("noteCategory");
            noteTimestamp = getArguments().getLong("noteTimestamp", -1);
            reminderTime = getArguments().getLong("noteReminderTime", -1);

            if (noteId != -1) {
                binding.editTextTitle.setText(title);
                binding.editTextContent.setText(content);
                binding.buttonSave.setText("Update Note");
                setCategoryChip(category);
                if (reminderTime != -1 && reminderTime > 0) {
                    reminderCalendar.setTimeInMillis(reminderTime);
                    updateReminderInfo();
                }
            }
        }

        binding.buttonSetReminder.setOnClickListener(v -> showDatePicker());
        binding.buttonRemoveReminder.setOnClickListener(v -> {
            reminderTime = -1;
            binding.textViewReminderInfo.setText("No reminder set");
            binding.buttonRemoveReminder.setVisibility(View.GONE);
        });

        binding.buttonSave.setOnClickListener(v -> {
            String title = binding.editTextTitle.getText().toString();
            String content = binding.editTextContent.getText().toString();
            String category = getSelectedCategory();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(getContext(), R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(getContext());
                long timestamp = (noteTimestamp != -1) ? noteTimestamp : System.currentTimeMillis();
                
                if (noteId == -1) {
                    db.noteDao().insert(new Note(title, content, timestamp, category, reminderTime));
                } else {
                    Note note = new Note(title, content, timestamp, category, reminderTime);
                    note.setId(noteId);
                    db.noteDao().update(note);
                }

                if (reminderTime > System.currentTimeMillis()) {
                    scheduleNotification(title, content, reminderTime);
                } else if (reminderTime == -1) {
                    cancelNotification();
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), R.string.note_saved_message, Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(SecondFragment.this).popBackStack();
                    });
                }
            });
        });

        View fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setVisibility(View.GONE);
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            reminderCalendar.set(Calendar.YEAR, year);
            reminderCalendar.set(Calendar.MONTH, month);
            reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker();
        }, reminderCalendar.get(Calendar.YEAR), reminderCalendar.get(Calendar.MONTH), reminderCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            reminderCalendar.set(Calendar.MINUTE, minute);
            reminderCalendar.set(Calendar.SECOND, 0);
            reminderTime = reminderCalendar.getTimeInMillis();
            updateReminderInfo();
        }, reminderCalendar.get(Calendar.HOUR_OF_DAY), reminderCalendar.get(Calendar.MINUTE), true).show();
    }

    private void updateReminderInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        binding.textViewReminderInfo.setText("Reminder: " + sdf.format(reminderCalendar.getTime()));
        binding.buttonRemoveReminder.setVisibility(View.VISIBLE);
    }

    private void scheduleNotification(String title, String content, long time) {
        Context context = getContext();
        if (context == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) time, intent, PendingIntent.FLAG_IMMUTABLE);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        }
    }

    private void cancelNotification() {
        Context context = getContext();
        if (context == null) return;
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, noteId, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private String getSelectedCategory() {
        int checkedChipId = binding.chipGroupCategory.getCheckedChipId();
        if (checkedChipId == R.id.chip_work) return "Work";
        if (checkedChipId == R.id.chip_personal) return "Personal";
        if (checkedChipId == R.id.chip_ideas) return "Ideas";
        return "General";
    }

    private void setCategoryChip(String category) {
        if (category == null) return;
        switch (category) {
            case "Work": binding.chipWork.setChecked(true); break;
            case "Personal": binding.chipPersonal.setChecked(true); break;
            case "Ideas": binding.chipIdeas.setChecked(true); break;
            default: binding.chipGeneral.setChecked(true); break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
