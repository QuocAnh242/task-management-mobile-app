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

    public interface OnCommentUpdatedListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnCommentDeletedListener {
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
                            .addOnSuccessListener(documentReference -> {
                                // Extract and send notifications to mentioned users
                                extractAndSendMentionNotifications(documentReference.getId(), taskId, content, finalUserName);
                                listener.onSuccess();
                            })
                            .addOnFailureListener(e -> listener.onError("Failed to create comment: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to get user data: " + e.getMessage()));
    }

    public void updateComment(String commentId, String content, OnCommentUpdatedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        // Get comment to check ownership and get taskId
        db.collection("comments").document(commentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onError("Comment not found");
                        return;
                    }

                    String userId = documentSnapshot.getString("userId");
                    if (!userId.equals(currentUser.getUid())) {
                        listener.onError("You can only edit your own comments");
                        return;
                    }

                    String taskId = documentSnapshot.getString("taskId");
                    String userName = documentSnapshot.getString("userName");

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("content", content);

                    db.collection("comments").document(commentId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                // Extract and send notifications to newly mentioned users
                                extractAndSendMentionNotifications(commentId, taskId, content, userName);
                                listener.onSuccess();
                            })
                            .addOnFailureListener(e -> listener.onError("Failed to update comment: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to get comment: " + e.getMessage()));
    }

    public void deleteComment(String commentId, OnCommentDeletedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        // Check ownership before deleting
        db.collection("comments").document(commentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onError("Comment not found");
                        return;
                    }

                    String userId = documentSnapshot.getString("userId");
                    if (!userId.equals(currentUser.getUid())) {
                        listener.onError("You can only delete your own comments");
                        return;
                    }

                    db.collection("comments").document(commentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onError("Failed to delete comment: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to get comment: " + e.getMessage()));
    }

    private void extractAndSendMentionNotifications(String commentId, String taskId, String content, String commenterName) {
        if (content == null || content.isEmpty() || taskId == null) {
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // Extract @mentions from content (format: @username)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        java.util.Set<String> mentionedUsernames = new java.util.HashSet<>();
        while (matcher.find()) {
            mentionedUsernames.add(matcher.group(1).toLowerCase());
        }

        if (mentionedUsernames.isEmpty()) {
            return;
        }

        // Get task and project info
        db.collection("tasks").document(taskId)
                .get()
                .addOnSuccessListener(taskDoc -> {
                    if (!taskDoc.exists()) return;
                    
                    String taskTitle = taskDoc.getString("title");
                    String projectId = taskDoc.getString("projectId");
                    
                    if (projectId == null) return;

                    // Get project members
                    db.collection("projects").document(projectId)
                            .get()
                            .addOnSuccessListener(projectDoc -> {
                                if (!projectDoc.exists()) return;
                                
                                List<String> memberIds = (List<String>) projectDoc.get("memberIds");
                                String leaderId = projectDoc.getString("leaderId");
                                
                                List<String> allUserIds = new ArrayList<>();
                                if (leaderId != null) allUserIds.add(leaderId);
                                if (memberIds != null) allUserIds.addAll(memberIds);
                                
                                // Match usernames to user IDs and send notifications
                                final int[] processed = {0};
                                final int total = allUserIds.size();
                                
                                for (String userId : allUserIds) {
                                    db.collection("users").document(userId)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                if (userDoc.exists()) {
                                                    String name = userDoc.getString("name");
                                                    String email = userDoc.getString("email");
                                                    
                                                    boolean isMentioned = false;
                                                    if (name != null) {
                                                        String nameLower = name.toLowerCase().replaceAll("\\s+", "");
                                                        if (mentionedUsernames.contains(nameLower)) {
                                                            isMentioned = true;
                                                        }
                                                    }
                                                    if (!isMentioned && email != null) {
                                                        String emailPrefix = email.split("@")[0].toLowerCase();
                                                        if (mentionedUsernames.contains(emailPrefix)) {
                                                            isMentioned = true;
                                                        }
                                                    }
                                                    
                                                    if (isMentioned && !userId.equals(currentUser.getUid())) {
                                                        Map<String, Object> notificationData = new HashMap<>();
                                                        notificationData.put("userId", userId);
                                                        notificationData.put("projectId", projectId);
                                                        notificationData.put("title", "You were mentioned in a comment");
                                                        notificationData.put("content", commenterName + " mentioned you in a comment on task: " + (taskTitle != null ? taskTitle : "Task"));
                                                        notificationData.put("status", "UNREAD");
                                                        notificationData.put("type", "COMMENT_MENTION");
                                                        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                                        notificationData.put("createdAt", createdAt);
                                                        db.collection("notifications").add(notificationData);
                                                    }
                                                }
                                                
                                                processed[0]++;
                                                if (processed[0] == total) {
                                                    // All users processed
                                                }
                                            });
                                }
                            });
                });
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

