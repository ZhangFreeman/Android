package com.android.my.zhang.dribbview.model;

import java.util.Date;


public class Comment {
    public String id;
    public String body;
    public int likes_count;
    public String likes_url;
    public Date created_at;
    public Date updated_at;
    public User user;
}
