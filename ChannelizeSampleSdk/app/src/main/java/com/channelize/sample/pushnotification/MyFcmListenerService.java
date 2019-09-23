/*
 *   Copyright (c) 2018 BigStep Technologies Private Limited.
 *
 *   The distribution of this source code is prohibited.
 */
package com.channelize.sample.pushnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.channelize.apisdk.utils.Logcat;
import com.channelize.sample.MainActivity;
import com.channelize.uisdk.ChannelizeUI;
import com.channelize.uisdk.interfaces.OnPushNotificationClearListener;
import com.channelize.uisdk.utils.ChannelizeUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


public class MyFcmListenerService extends FirebaseMessagingService implements OnPushNotificationClearListener {

    // Member variables.
    public static int messengerCounter = 1;
    public static NotificationManager messengerNotificationsManager;
    public static Map<Integer, String> messengerMap = new TreeMap<>(Collections.reverseOrder());
    private boolean isGroup;
    private String chatRoomId;
    private int singleChatMessageCount, totalMessageCount;
    private ChannelizeUtils channelizeUtils;
    public static final Map<String, PushNotificationModel> NOTIFICATION_CHATS_MAP = new ConcurrentHashMap<>();
    public static final Map<String, Notification> NOTIFICATION_MAP = new ConcurrentHashMap<>();


    @Override
    public void onNewToken(String token) {
        registerRefreshedToken(token);
        super.onNewToken(token);
    }

    /**
     * Method to Update FCM token on server when the token is refreshed.
     *
     * @param token Updated FCM token.
     */
    public void registerRefreshedToken(final String token) {

    }

    /**
     * Called when message is received.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map data = remoteMessage.getData();
        Logcat.d(MyFcmListenerService.class, "onMessageReceived, data: " + data);

        boolean isChannelizeNotification = data.containsKey("messageType") && data.get("messageType").equals("primemessenger");

        if (isChannelizeNotification) {
            ChannelizeUI.getInstance().processPushNotification(getApplicationContext(), remoteMessage);

        }
    }

    /**
     * Create and show a simple notification containing the received FCM message and title.
     *
     * @param context Context of application.
     * @param body    FCM message received.
     * @param title   Title of FCM notification in case of single notification.
     */
    private void generateCustomNotificationForMessenger(Context context, String body, String title) {
        List<String> msgList = new ArrayList<>();
        if (NOTIFICATION_CHATS_MAP.get(chatRoomId) != null) {
            msgList = NOTIFICATION_CHATS_MAP.get(chatRoomId).getMsgList();
        }
        msgList.add(body != null ? body : "");
        NOTIFICATION_CHATS_MAP.put(chatRoomId, new PushNotificationModel(title, isGroup, singleChatMessageCount, msgList));
        MainActivity.generatePushNotifications(context);
    }

    /**
     * Method to clear messenger's push notification when clicking on it.
     */
    public static void clearMessengerPushNotification() {
        if (messengerNotificationsManager != null && messengerMap != null) {
            messengerNotificationsManager.cancelAll();
            messengerMap.clear();
            messengerCounter = 1;
        }
    }

    public static void cancelNotification(int notificationId) {
        if (messengerNotificationsManager != null) {
            messengerNotificationsManager.cancel(notificationId);
        }
    }

    public static void clearNotifications(Context context) {
        messengerNotificationsManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        clearMessengerPushNotification();
    }

    @Override
    public void clearPushNotifications() {
        clearMessengerPushNotification();
    }

}
