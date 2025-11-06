package com.prm392.taskmanaapp.data;

public class OAuthAccount {
    private int oauthaccountId;
    private int userId; // FK to user_app
    private String provider; // Google, GitHub, Facebook, etc.
    private String providerUserId;
    private String accountEmail;
    private String createdAt;

    public OAuthAccount() {}

    public OAuthAccount(int oauthaccountId, int userId, String provider,
                       String providerUserId, String accountEmail, String createdAt) {
        this.oauthaccountId = oauthaccountId;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.accountEmail = accountEmail;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getOauthaccountId() { return oauthaccountId; }
    public void setOauthaccountId(int oauthaccountId) { this.oauthaccountId = oauthaccountId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    public String getAccountEmail() { return accountEmail; }
    public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

