package com.dreamcode.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamcode.task.data.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Note note);
    }

    public interface OnEditClickListener {
        void onEditClick(Note note);
    }

    public NoteAdapter(OnDeleteClickListener deleteClickListener, OnEditClickListener editClickListener) {
        this.deleteClickListener = deleteClickListener;
        this.editClickListener = editClickListener;
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
                }
                return false;
            });
            popup.show();
        });
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
