package com.dreamcode.task;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamcode.task.data.Note;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;
    private OnItemClickListener itemClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Note note);
    }

    public interface OnEditClickListener {
        void onEditClick(Note note);
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public NoteAdapter(OnDeleteClickListener deleteClickListener, OnEditClickListener editClickListener, OnItemClickListener itemClickListener) {
        this.deleteClickListener = deleteClickListener;
        this.editClickListener = editClickListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.textViewTitle.setText(currentNote.getTitle());
        holder.textViewContent.setText(currentNote.getContent());
        holder.textViewCategory.setText(currentNote.getCategory());
        
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentNote);
            }
        });

        holder.buttonOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_note_options, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    if (editClickListener != null) {
                        editClickListener.onEditClick(currentNote);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(currentNote);
                    }
                    return true;
                } else if (itemId == R.id.action_share) {
                    showShareDialog(v.getContext(), currentNote);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void showShareDialog(Context context, Note note) {
        String[] options = {"Share as Text", "Share as Image"};
        new AlertDialog.Builder(context)
                .setTitle("Share Note")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        shareNoteAsText(context, note);
                    } else {
                        shareNoteAsImage(context, note);
                    }
                })
                .show();
    }

    private void shareNoteAsText(Context context, Note note) {
        String shareText = note.getTitle() + "\n\n" + note.getContent();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, null));
    }

    private void shareNoteAsImage(Context context, Note note) {
        View shareView = LayoutInflater.from(context).inflate(R.layout.layout_share_note, null);
        ((TextView) shareView.findViewById(R.id.share_title)).setText(note.getTitle());
        ((TextView) shareView.findViewById(R.id.share_content)).setText(note.getContent());

        shareView.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        shareView.layout(0, 0, shareView.getMeasuredWidth(), shareView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(shareView.getMeasuredWidth(), shareView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        shareView.draw(canvas);

        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "note_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(context, "com.dreamcode.task.fileprovider", file);
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        } catch (IOException e) {
            Toast.makeText(context, "Error sharing image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewContent;
        private TextView textViewCategory;
        private ImageButton buttonOptions;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewContent = itemView.findViewById(R.id.text_view_content);
            textViewCategory = itemView.findViewById(R.id.text_view_category);
            buttonOptions = itemView.findViewById(R.id.button_options);
        }
    }
}
