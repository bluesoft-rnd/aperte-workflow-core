package org.aperteworkflow.admin.controller;

import java.util.Date;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-05-14.
 */
public class CommentTime {
    private Date time;
    private String formattedTime;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
}
