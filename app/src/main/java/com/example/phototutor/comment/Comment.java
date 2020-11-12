package com.example.phototutor.comment;

import java.time.ZonedDateTime;
import java.util.Date;

public class Comment {
    private int photoId;
    private String comment;
    private int userId;
    private Date createdTime;

    public Comment(int photoId, String comment, int userId, String strDate) {
        ZonedDateTime dateTime = ZonedDateTime.parse(strDate);
        createdTime = Date.from(dateTime.toInstant());
        this.photoId = photoId;
        this.comment = comment;
        this.userId = userId;
    }

    public int getPhotoId() {
        return photoId;
    }

    public String getComment() {
        return comment;
    }

    public int getUserId() {
        return userId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }
}
