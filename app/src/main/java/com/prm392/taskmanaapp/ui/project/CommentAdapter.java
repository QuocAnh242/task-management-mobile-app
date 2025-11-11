package com.prm392.taskmanaapp.ui.project;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (content != null && !content.isEmpty()) {
            SpannableString spannable = highlightMentions(content);
            holder.tvContent.setText(spannable);
            holder.tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.tvContent.setText("");
        }
        
        // Set avatar initial (first letter of name)
        String authorName = comment.getUserName();
        if (authorName != null && !authorName.isEmpty()) {
            String initial = authorName.substring(0, 1).toUpperCase();
            holder.tvAvatar.setText(initial);
        } else {
            holder.tvAvatar.setText("👤");
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
        if (comments == null) {
            this.comments = new ArrayList<>();
        } else {
            // Remove duplicates based on commentId
            Map<String, Comment> uniqueComments = new HashMap<>();
            for (Comment comment : comments) {
                if (comment != null && comment.getCommentId() != null) {
                    uniqueComments.put(comment.getCommentId(), comment);
                }
            }
            this.comments = new ArrayList<>(uniqueComments.values());
            // Sort by createdAt
            this.comments.sort((c1, c2) -> {
                if (c1.getCreatedAt() == null && c2.getCreatedAt() == null) return 0;
                if (c1.getCreatedAt() == null) return 1;
                if (c2.getCreatedAt() == null) return -1;
                return c1.getCreatedAt().compareTo(c2.getCreatedAt());
            });
        }
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

    /**
     * Highlight @mentions in comment text
     */
    private SpannableString highlightMentions(String text) {
        SpannableString spannable = new SpannableString(text);
        
        // Pattern to match @username or @email
        Pattern mentionPattern = Pattern.compile("@(\\w+|[\\w.-]+@[\\w.-]+\\.[\\w]+)");
        Matcher matcher = mentionPattern.matcher(text);
        
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            
            // Highlight mention with primary color
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#2196F3"));
            spannable.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            // Make it clickable (you can add click listener here if needed)
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // Handle mention click if needed
                }
                
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        return spannable;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor;
        TextView tvContent;
        TextView tvTime;
        TextView tvAvatar;
        ImageButton btnEdit;
        ImageButton btnDelete;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            tvAvatar = itemView.findViewById(R.id.tvCommentAvatar);
            btnEdit = itemView.findViewById(R.id.btnEditComment);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}

