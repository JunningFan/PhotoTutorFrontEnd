package com.example.phototutor.notification;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Date;

public class Notification {
    private int notificationId;
    private int userId;
    private int actorId;


    private String type;
    private String message;

    private ZonedDateTime createTime;
    public Notification(int notificationId, int userId, int actorId, String type, String message,String createTime) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.actorId = actorId;
        this.type = type;
        this.message = message;
        this.createTime = ZonedDateTime.parse(createTime);
    }


    public int getNotificationId() {
        return notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public int getActorId() {
        return actorId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }


    public ZonedDateTime getCreateTime() {
        return createTime;
    }
}
