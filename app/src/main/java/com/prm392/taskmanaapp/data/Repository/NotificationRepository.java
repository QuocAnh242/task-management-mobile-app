package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.taskmanaapp.data.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationRepository {

    public interface OnNotificationsLoadedListener {
        void onSuccess(List<Notification> notifications);
        void onError(String message);
    }

    public interface OnNotificationCreatedListener {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Create a notification for a user
     */
    public void createNotification(String userId, String projectId, String title, 
                                   String content, String type, OnNotificationCreatedListener listener) {
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("projectId", projectId != null ? projectId : "");
        notificationData.put("title", title);
        notificationData.put("content", content);
        notificationData.put("status", "UNREAD");
        notificationData.put("type", type);
        notificationData.put("createdAt", createdAt);

        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to create notification: " + e.getMessage());
                });
    }

    /**
     * Load notifications for current user
     */
    public void loadNotifications(OnNotificationsLoadedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        // Load all notifications and filter/sort in memory to avoid index requirement
        db.collection("notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = documentToNotification(doc);
                        notifications.add(notification);
                    }
                    
                    // Sort by createdAt descending (newest first)
                    notifications.sort((n1, n2) -> {
                        if (n1.getCreatedAt() == null && n2.getCreatedAt() == null) return 0;
                        if (n1.getCreatedAt() == null) return 1;
                        if (n2.getCreatedAt() == null) return -1;
                        return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                    });
                    
                    // Limit to 20 most recent
                    if (notifications.size() > 20) {
                        notifications = notifications.subList(0, 20);
                    }
                    
                    listener.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    // If index error, try without orderBy
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("index")) {
                        // Try loading without orderBy
                        db.collection("notifications")
                                .whereEqualTo("userId", currentUser.getUid())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<Notification> notifications = new ArrayList<>();
                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                        Notification notification = documentToNotification(doc);
                                        notifications.add(notification);
                                    }
                                    
                                    // Sort in memory
                                    notifications.sort((n1, n2) -> {
                                        if (n1.getCreatedAt() == null && n2.getCreatedAt() == null) return 0;
                                        if (n1.getCreatedAt() == null) return 1;
                                        if (n2.getCreatedAt() == null) return -1;
                                        return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                                    });
                                    
                                    if (notifications.size() > 20) {
                                        notifications = notifications.subList(0, 20);
                                    }
                                    
                                    listener.onSuccess(notifications);
                                })
                                .addOnFailureListener(e2 -> {
                                    listener.onError("Failed to load notifications: " + e2.getMessage());
                                });
                    } else {
                        listener.onError("Failed to load notifications: " + e.getMessage());
                    }
                });
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId) {
        db.collection("notifications")
                .document(notificationId)
                .update("status", "READ")
                .addOnFailureListener(e -> {
                    // Silent fail - just log
                    android.util.Log.e("NotificationRepository", "Failed to mark as read: " + e.getMessage());
                });
    }

    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId) {
        db.collection("notifications")
                .document(notificationId)
                .delete()
                .addOnFailureListener(e -> {
                    android.util.Log.e("NotificationRepository", "Failed to delete notification: " + e.getMessage());
                });
    }

    private Notification documentToNotification(QueryDocumentSnapshot doc) {
        Notification notification = new Notification();
        notification.setNotificationId(doc.getId());
        notification.setUserId(doc.getString("userId"));
        notification.setProjectId(doc.getString("projectId"));
        notification.setTitle(doc.getString("title"));
        notification.setContent(doc.getString("content"));
        notification.setStatus(doc.getString("status"));
        notification.setType(doc.getString("type"));
        notification.setCreatedAt(doc.getString("createdAt"));
        return notification;
    }
}

