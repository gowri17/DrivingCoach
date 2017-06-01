package com.drava.android.fcm;

import java.io.Serializable;

public class PushNotification implements Serializable {
    
    private static final long serialVersionUID = -1958362199159185830L;
    public int productCount, userId, type;
    public String message;

    @Override
    public String toString() {
        return "PushNotification{" +
                "productCount=" + productCount +
                ", userId=" + userId +
                ", type=" + type +
                ", message='" + message + '\'' +
                '}';
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PushNotification(int productCount, String message,
                            int userId, int type) {
        super();
        this.productCount = productCount;
        this.message = message;
        this.userId = userId;
        this.type = type;
    }
}
