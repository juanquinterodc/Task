package com.dreamcode.task;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dreamcode.task.data.AppDatabase;
import com.dreamcode.task.data.Note;
import com.dreamcode.task.databinding.FragmentViewNoteBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ViewNoteFragment extends Fragment {

    private FragmentViewNoteBinding binding;
    private int noteId;
    private String noteTitle;
    private String noteContent;
    private String noteCategory;
    private long noteTimestamp;
    private long noteReminderTime;
    private boolean noteIsChecklist;
    private boolean noteIsSecret;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewNoteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            noteId = getArguments().getInt("noteId");
            noteTitle = getArguments().getString("noteTitle");
            noteContent = getArguments().getString("noteContent");
            noteCategory = getArguments().getString("noteCategory");
            noteTimestamp = getArguments().getLong("noteTimestamp");
            noteReminderTime = getArguments().getLong("noteReminderTime");
            noteIsChecklist = getArguments().getBoolean("noteIsChecklist");
            noteIsSecret = getArguments().getBoolean("noteIsSecret");

            binding.textViewViewTitle.setText(noteTitle);
            binding.textViewViewCategory.setText(noteCategory);
            
            loadContent();
        }

        binding.buttonViewEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("noteId", noteId);
            bundle.putString("noteTitle", noteTitle);
            bundle.putString("noteContent", noteContent);
            bundle.putString("noteCategory", noteCategory);
            bundle.putLong("noteTimestamp", noteTimestamp);
            bundle.putLong("noteReminderTime", noteReminderTime);
            bundle.putBoolean("noteIsChecklist", noteIsChecklist);
            bundle.putBoolean("noteIsSecret", noteIsSecret);
            NavHostFragment.findNavController(this).navigate(R.id.action_ViewNoteFragment_to_SecondFragment, bundle);
        });

        binding.buttonViewShare.setOnClickListener(v -> showShareDialog());

        binding.buttonViewDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.delete_confirmation)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            Note note = new Note(noteTitle, noteContent, noteTimestamp, noteCategory, noteReminderTime, noteIsChecklist, noteIsSecret);
                            note.setId(noteId);
                            AppDatabase.getDatabase(getContext()).noteDao().delete(note);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    NavHostFragment.findNavController(this).popBackStack(R.id.FirstFragment, false);
                                });
                            }
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        View fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) fab.setVisibility(View.GONE);
    }

    private void loadContent() {
        if (noteContent != null) {
            if (noteIsChecklist) {
                binding.textViewViewContent.setVisibility(View.GONE);
                binding.layoutChecklistItems.setVisibility(View.VISIBLE);
                renderChecklist(noteContent);
            } else {
                binding.textViewViewContent.setVisibility(View.VISIBLE);
                binding.layoutChecklistItems.setVisibility(View.GONE);
                binding.textViewViewContent.setText(Html.fromHtml(noteContent, Html.FROM_HTML_MODE_LEGACY));
            }
        }
    }

    private void renderChecklist(String htmlContent) {
        binding.layoutChecklistItems.removeAllViews();
        String plainText = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY).toString();
        String[] lines = plainText.split("\n");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            boolean isChecked = line.startsWith("☑");
            String itemText = line.replace("☐ ", "").replace("☑ ", "").trim();
            
            CheckBox cb = new CheckBox(getContext());
            cb.setText(itemText);
            cb.setChecked(isChecked);
            cb.setPadding(8, 16, 8, 16);
            
            cb.setOnCheckedChangeListener((buttonView, checked) -> {
                updateChecklistState();
            });
            
            binding.layoutChecklistItems.addView(cb);
        }
    }

    private void updateChecklistState() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < binding.layoutChecklistItems.getChildCount(); i++) {
            CheckBox cb = (CheckBox) binding.layoutChecklistItems.getChildAt(i);
            sb.append(cb.isChecked() ? "☑ " : "☐ ").append(cb.getText()).append("\n");
        }
        
        // Convert updated state back to HTML for storage
        noteContent = Html.toHtml(new SpannableString(sb.toString().trim()), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            Note note = new Note(noteTitle, noteContent, noteTimestamp, noteCategory, noteReminderTime, noteIsChecklist, noteIsSecret);
            note.setId(noteId);
            db.noteDao().update(note);
        });
    }

    private void showShareDialog() {
        String[] options = {getString(R.string.share_option_text), getString(R.string.share_option_image)};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_share_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        shareNoteAsText();
                    } else {
                        shareNoteAsImage();
                    }
                })
                .show();
    }

    private void shareNoteAsText() {
        String plainText = Html.fromHtml(noteContent, Html.FROM_HTML_MODE_LEGACY).toString();
        String shareText = noteTitle + "\n\n" + plainText;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }

    private void shareNoteAsImage() {
        View shareView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_share_note, null);
        ((TextView) shareView.findViewById(R.id.share_title)).setText(noteTitle);
        
        TextView contentTv = shareView.findViewById(R.id.share_content);
        if (noteIsChecklist) {
            String plainText = Html.fromHtml(noteContent, Html.FROM_HTML_MODE_LEGACY).toString();
            contentTv.setText(plainText);
        } else {
            contentTv.setText(Html.fromHtml(noteContent, Html.FROM_HTML_MODE_LEGACY));
        }

        shareView.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        shareView.layout(0, 0, shareView.getMeasuredWidth(), shareView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(shareView.getMeasuredWidth(), shareView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        shareView.draw(canvas);

        try {
            File cachePath = new File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "note_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.dreamcode.task.fileprovider", file);
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, requireContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), R.string.error_sharing_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
