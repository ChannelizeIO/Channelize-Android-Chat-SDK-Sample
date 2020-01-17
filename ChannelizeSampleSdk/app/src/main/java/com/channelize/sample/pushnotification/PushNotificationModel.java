/*
 *   Copyright (c) 2018 BigStep Technologies Private Limited.
 *
 *   The distribution of this source code is prohibited.
 */

package com.channelize.sample.pushnotification;


import java.util.List;

public class PushNotificationModel {

    // Member variables.
    private String title;
    private boolean isGroupChat;
    private int newMessageCount, notificationId;
    private List<String> msgList;


    public PushNotificationModel(String title, boolean isGroupChat, int newMessageCount, List<String> msgList) {
        this.title = title;
        this.isGroupChat = isGroupChat;
        this.newMessageCount = newMessageCount;
        this.msgList = msgList;
    }

    public String getTitle() {
        return title;
    }

    public boolean isGroupChat() {
        return isGroupChat;
    }

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public List<String> getMsgList() {
        return msgList;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    @Override
    public String toString() {
        return "PushNotificationModel{" +
                "title='" + title + '\'' +
                ", isGroupChat=" + isGroupChat +
                ", newMessageCount=" + newMessageCount +
                '}';
    }
}
