package com.dreamcode.task;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
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
    private boolean isInternalEdit = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            boolean isChecklist = getArguments().getBoolean("noteIsChecklist", false);
            boolean isSecret = getArguments().getBoolean("noteIsSecret", false);

            if (noteId != -1) {
                binding.editTextTitle.setText(title);
                if (content != null) {
                    binding.editTextContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                }
                binding.buttonSave.setText("Update Note");
                binding.switchChecklist.setChecked(isChecklist);
                binding.switchSecret.setChecked(isSecret);
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

        binding.buttonBold.setOnClickListener(v -> applyStyle(android.graphics.Typeface.BOLD));
        binding.buttonItalic.setOnClickListener(v -> applyStyle(android.graphics.Typeface.ITALIC));

        // checklist mode logic
        binding.switchChecklist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                convertCurrentToChecklist();
            }
        });

        binding.editTextContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInternalEdit || !binding.switchChecklist.isChecked()) return;
                
                // If user pressed enter, auto-add a checkbox
                if (count == 1 && s.charAt(start) == '\n') {
                    isInternalEdit = true;
                    binding.editTextContent.getText().insert(start + 1, "☐ ");
                    isInternalEdit = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.buttonSave.setOnClickListener(v -> {
            String title = binding.editTextTitle.getText().toString();
            // preserve formatting
            String content = Html.toHtml(binding.editTextContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            String category = getSelectedCategory();
            boolean isChecklist = binding.switchChecklist.isChecked();
            boolean isSecret = binding.switchSecret.isChecked();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(binding.editTextContent.getText())) {
                Toast.makeText(getContext(), R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(getContext());
                long timestamp = (noteTimestamp != -1) ? noteTimestamp : System.currentTimeMillis();
                
                Note note = new Note(title, content, timestamp, category, reminderTime, isChecklist, isSecret);
                if (noteId != -1) note.setId(noteId);

                if (noteId == -1) {
                    db.noteDao().insert(note);
                } else {
                    db.noteDao().update(note);
                }

                if (reminderTime > System.currentTimeMillis()) {
                    scheduleNotification(title, "Reminder for your note", reminderTime);
                } else if (reminderTime == -1) {
                    cancelNotification();
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), R.string.note_saved_message, Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(SecondFragment.this).popBackStack(R.id.FirstFragment, false);
                    });
                }
            });
        });

        View fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) fab.setVisibility(View.GONE);
    }

    private void convertCurrentToChecklist() {
        String text = binding.editTextContent.getText().toString();
        if (text.isEmpty()) {
            binding.editTextContent.setText("☐ ");
            binding.editTextContent.setSelection(2);
            return;
        }

        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!line.startsWith("☐ ") && !line.startsWith("☑ ")) {
                sb.append("☐ ").append(line).append("\n");
            } else {
                sb.append(line).append("\n");
            }
        }
        isInternalEdit = true;
        binding.editTextContent.setText(sb.toString().trim());
        isInternalEdit = false;
        // set selection to end
        binding.editTextContent.setSelection(binding.editTextContent.getText().length());
    }

    private void applyStyle(int style) {
        int start = binding.editTextContent.getSelectionStart();
        int end = binding.editTextContent.getSelectionEnd();
        if (start < end) {
            Spannable spannable = binding.editTextContent.getText();
            spannable.setSpan(new StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) (time / 1000), intent, PendingIntent.FLAG_IMMUTABLE);
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
        if (alarmManager != null) alarmManager.cancel(pendingIntent);
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
