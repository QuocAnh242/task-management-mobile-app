package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.taskmanaapp.data.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentRepository {

    public interface OnCommentsLoadedListener {
        void onSuccess(List<Comment> comments);
        void onError(String message);
    }

    public interface OnCommentCreatedListener {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public CommentRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void loadComments(String taskId, OnCommentsLoadedListener listener) {
        db.collection("comments")
                .whereEqualTo("taskId", taskId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = documentToComment(doc);
                        comments.add(comment);
                    }
                    listener.onSuccess(comments);
                })
                .addOnFailureListener(e -> {
                    // If index error, try without orderBy
                    db.collection("comments")
                            .whereEqualTo("taskId", taskId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<Comment> comments = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    Comment comment = documentToComment(doc);
                                    comments.add(comment);
                                }
                                // Sort in memory
                                comments.sort((c1, c2) -> {
                                    if (c1.getCreatedAt() == null && c2.getCreatedAt() == null) return 0;
                                    if (c1.getCreatedAt() == null) return 1;
                                    if (c2.getCreatedAt() == null) return -1;
                                    return c1.getCreatedAt().compareTo(c2.getCreatedAt());
                                });
                                listener.onSuccess(comments);
                            })
                            .addOnFailureListener(e2 -> listener.onError("Failed to load comments: " + e2.getMessage()));
                });
    }

    public void createComment(String taskId, String content, OnCommentCreatedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        // Get user name
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userDoc.getString("name");
                    if (userName == null || userName.isEmpty()) {
                        userName = currentUser.getEmail();
                    }
                    final String finalUserName = userName;

                    String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("taskId", taskId);
                    commentData.put("userId", currentUser.getUid());
                    commentData.put("userName", finalUserName);
                    commentData.put("content", content);
                    commentData.put("createdAt", createdAt);

                    db.collection("comments")
                            .add(commentData)
                            .addOnSuccessListener(documentReference -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onError("Failed to create comment: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to get user data: " + e.getMessage()));
    }

    private Comment documentToComment(QueryDocumentSnapshot doc) {
        Comment comment = new Comment();
        comment.setCommentId(doc.getId());
        comment.setTaskId(doc.getString("taskId"));
        comment.setUserId(doc.getString("userId"));
        comment.setUserName(doc.getString("userName"));
        comment.setContent(doc.getString("content"));
        comment.setCreatedAt(doc.getString("createdAt"));
        return comment;
    }
}

