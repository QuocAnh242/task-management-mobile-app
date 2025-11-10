package com.prm392.taskmanaapp.ui.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Comment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private OnCommentActionListener actionListener;
    private FirebaseAuth mAuth;

    public interface OnCommentActionListener {
        void onEditComment(Comment comment);
        void onDeleteComment(Comment comment);
    }

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void setActionListener(OnCommentActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvAuthor.setText(comment.getUserName());
        
        // Format content to highlight @mentions
        String content = comment.getContent();
        if (content != null) {
            // Highlight @mentions with SpannableString (simplified - just show text for now)
            holder.tvContent.setText(content);
        } else {
            holder.tvContent.setText("");
        }
        
        holder.tvTime.setText(formatTime(comment.getCreatedAt()));
        
        // Show edit/delete buttons only for current user's comments
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isOwner = currentUser != null && currentUser.getUid().equals(comment.getUserId());
        
        if (isOwner && actionListener != null) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            
            holder.btnEdit.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onEditComment(comment);
                }
            });
            
            holder.btnDelete.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onDeleteComment(comment);
                }
            });
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public void updateComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    private String formatTime(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            if (date == null) return "";

            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + "d ago";
            } else if (hours > 0) {
                return hours + "h ago";
            } else if (minutes > 0) {
                return minutes + "m ago";
            } else {
                return "Just now";
            }
        } catch (ParseException e) {
            return createdAt;
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor;
        TextView tvContent;
        TextView tvTime;
        ImageButton btnEdit;
        ImageButton btnDelete;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            btnEdit = itemView.findViewById(R.id.btnEditComment);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}

