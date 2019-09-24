/*
 *   Copyright (c) 2018 BigStep Technologies Private Limited.
 *
 *   The distribution of this source code is prohibited.
 */
package com.channelize.sample.pushnotification;

import com.channelize.apisdk.utils.Logcat;
import com.channelize.uisdk.ChannelizeUI;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class MyFcmListenerService extends FirebaseMessagingService {


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

}
