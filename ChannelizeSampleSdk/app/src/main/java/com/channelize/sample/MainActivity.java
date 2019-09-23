package com.channelize.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.channelize.apisdk.Channelize;
import com.channelize.apisdk.network.response.ChannelizeError;
import com.channelize.apisdk.network.response.CompletionHandler;
import com.channelize.apisdk.network.response.LoginResponse;
import com.channelize.apisdk.utils.ChannelizePreferences;
import com.channelize.apisdk.utils.Logcat;
import com.channelize.sample.pushnotification.MyFcmListenerService;
import com.channelize.sample.pushnotification.PushNotificationModel;
import com.channelize.uisdk.ChannelizeMainActivity;
import com.channelize.uisdk.Constants;
import com.channelize.uisdk.utils.ChannelizeUtils;
import com.google.firebase.iid.FirebaseInstanceId;


import java.io.IOException;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private Context mContext;
    private ProgressDialog progressDialog;
    private Channelize channelize;
    private ChannelizeUtils channelizeUtils;
    private static final String CHANNEL_ID = "pm_channel_123";
    MyListData[] myListData;
    private RelativeLayout loggedInView;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        channelize = Channelize.getInstance();

        loggedInView = findViewById(R.id.logged_in_view);
        recyclerView = findViewById(R.id.recycler_view);

        String currentUserId = ChannelizePreferences.getCurrentUserId(mContext);
        channelizeUtils = ChannelizeUtils.getInstance();
        if (currentUserId != null && !currentUserId.isEmpty()
                && !currentUserId.equals("null")) {
            channelize.setCurrentUserId(currentUserId);
            Channelize.getInstance().setCurrentUserId(currentUserId);

            setLoggedInUserView();

            startChannelize(null);

            TextView user = findViewById(R.id.title);
            user.setText("Welcome " + channelize.getCurrentUserId());

            TextView logoutButton = findViewById(R.id.logout_button);
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Logout().execute();
                }
            });

        } else {
            setLoggedOutUserView();
        }
        channelizeUtils.setAppActiveStatus(true);


        progressDialog = new ProgressDialog(mContext);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        initializeListData();

        MyListAdapter adapter = new MyListAdapter(myListData, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                MyListData myData = myListData[position];
                doLoginInChannelize(myData.getEmail(), myData.getPassword());
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void initializeListData() {
        myListData = new MyListData[] {
                new MyListData("test1@channelize.io", "123456"),
                new MyListData("test2@channelize.io", "123456"),
                new MyListData("test3@channelize.io", "123456"),
                new MyListData("test4@channelize.io", "123456"),
                new MyListData("test5@channelize.io", "123456"),
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        channelizeUtils.setAppActiveStatus(true);
    }


    private void doLoginInChannelize(String email, String password) {
        progressDialog.setMessage("Logging-In ...");
        progressDialog.show();
        channelize.loginWithEmailPassword(email, password, new CompletionHandler<LoginResponse>() {
            @Override
            public void onComplete(LoginResponse result, ChannelizeError error) {
                progressDialog.dismiss();
                if (result != null && result.getUser() != null) {
                    startChannelize(null);
                } else if (error != null) {
                    Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startChannelize(Bundle chatInfo) {

        new FirebaseToken(true).execute();
        Channelize.connect();
        Intent intent = new Intent(mContext, ChannelizeMainActivity.class);
        intent.putExtra("package_name", mContext.getPackageName());
        if (chatInfo != null) {
            intent.putExtras(chatInfo);
        }
        startActivity(intent);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLoggedInUserView();
            }
        });

    }


    public void setLoggedInUserView() {
        loggedInView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }


    public void setLoggedOutUserView() {
        loggedInView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }


    /* Executing background task for sending post request for sing out */
    public class Logout extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Logging out ...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Channelize.logout(null);
            channelizeUtils.clearCache();
            MyFcmListenerService.clearMessengerPushNotification();
            new FirebaseToken(false).execute();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            channelize.setCurrentUserId("");
            setLoggedOutUserView();
        }
    }

    public class FirebaseToken extends AsyncTask<Void, Boolean, String> {

        // Member variables.
        private boolean isGenerateToken = false;

        public FirebaseToken(boolean isGenerateToken) {
            this.isGenerateToken = isGenerateToken;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                if (isGenerateToken) {
                    Logcat.d(MainActivity.class, "Sender Id: "+ Config.FIREBASE_SENDER_ID);
                    return FirebaseInstanceId.getInstance().getToken(Config.FIREBASE_SENDER_ID, "FCM");

                } else {
                    FirebaseInstanceId.getInstance().deleteToken(Config.FIREBASE_SENDER_ID, "FCM");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            Logcat.d(MainActivity.class, "Firebase Token: " + token);
            if (isGenerateToken) {
                channelize.registerFcmToken(token);
            }
        }
    }

    public static void generatePushNotifications(Context context) {
        if (!MyFcmListenerService.NOTIFICATION_CHATS_MAP.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                showPushNotificationsOnNougat(context);
            } else {
                showPushNotificationsOnPreNougat(context);
            }
        }
    }

    private static void showPushNotificationsOnPreNougat(Context context) {
        if (MyFcmListenerService.messengerNotificationsManager == null) {
            MyFcmListenerService.messengerNotificationsManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        boolean isGroup = false;
        String chatRoomId = null, title = null, message;
        int singleChatMessageCount = 0;
        int totalMessageCount = 0;
        if (!MyFcmListenerService.NOTIFICATION_CHATS_MAP.isEmpty()) {
            for (Map.Entry<String, PushNotificationModel> chatMessages : MyFcmListenerService.NOTIFICATION_CHATS_MAP.entrySet()) {
                PushNotificationModel pushNotificationModel = chatMessages.getValue();
                chatRoomId = chatMessages.getKey();
                title = pushNotificationModel.getTitle();
                singleChatMessageCount = pushNotificationModel.getNewMessageCount();
                isGroup = pushNotificationModel.isGroupChat();
                for (String msg : pushNotificationModel.getMsgList()) {
                    totalMessageCount++;
                    inboxStyle.addLine(pushNotificationModel.getTitle() + ": " + msg);
                }
            }
        }

        String summaryText = getSummaryText(context, totalMessageCount);
        Logcat.d(MainActivity.class, "SummaryText: " + summaryText);
        inboxStyle.setSummaryText(summaryText);

        Intent notificationIntent;
        if (ChannelizeMainActivity.IS_RECENT_CHAT_VISIBLE) {
            notificationIntent = new Intent(context, ChannelizeMainActivity.class);
        } else {
            notificationIntent = new Intent(context, MainActivity.class);
        }

        notificationIntent.putExtra(Constants.CHAT_ID, chatRoomId);
        notificationIntent.putExtra(Constants.IS_GROUP_CHAT, isGroup);
        notificationIntent.putExtra(Constants.IS_SINGLE_CHAT, (MyFcmListenerService.NOTIFICATION_CHATS_MAP.size() == 1));
        notificationIntent.putExtra(Constants.CHAT_ROOM_TITLE, title);
        notificationIntent.putExtra("isMessenger", true);
        notificationIntent.putExtra("package_name", context.getPackageName());
        if (ChannelizeMainActivity.IS_RECENT_CHAT_VISIBLE) {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        PendingIntent intent = PendingIntent.getActivity(context, 0 /* Request code */,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "channel_id_" + "123");
        Notification notification = mBuilder
                .setWhen(System.currentTimeMillis())
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(summaryText)
                .setColor(ContextCompat.getColor(context, R.color.themeButtonColor))
                .setSmallIcon(R.drawable.push_noti_icon)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setContentIntent(intent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .setStyle(inboxStyle)
                .build();
        MyFcmListenerService.messengerNotificationsManager.notify(0, notification);
    }

    private static void showPushNotificationsOnNougat(Context context) {
        //use constant ID for notification used as group summary
        int SUMMARY_ID = 0;
        final String GROUP_KEY = context.getPackageName() + "MESSENGER_PUSH_NOTIFICATION";

        MyFcmListenerService.NOTIFICATION_MAP.clear();
        if (MyFcmListenerService.messengerNotificationsManager == null) {
            MyFcmListenerService.messengerNotificationsManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        int totalMessageCount = 0;
        if (!MyFcmListenerService.NOTIFICATION_CHATS_MAP.isEmpty()) {
            for (Map.Entry<String, PushNotificationModel> chatMessages : MyFcmListenerService.NOTIFICATION_CHATS_MAP.entrySet()) {
                Intent notificationIntent;
                if (ChannelizeMainActivity.IS_RECENT_CHAT_VISIBLE) {
                    notificationIntent = new Intent(context, ChannelizeMainActivity.class);
                } else {
                    notificationIntent = new Intent(context, MainActivity.class);
                }

                notificationIntent.putExtra(Constants.IS_REDIRECTED_FROM_PUSH_NOTIFICATION, true);
                notificationIntent.putExtra(Constants.CHAT_ID, chatMessages.getKey());
                PushNotificationModel pushNotificationModel = chatMessages.getValue();
                notificationIntent.putExtra(Constants.IS_GROUP_CHAT, pushNotificationModel.isGroupChat());
                notificationIntent.putExtra(Constants.IS_SINGLE_CHAT, true);
                notificationIntent.putExtra(Constants.CHAT_ROOM_TITLE, pushNotificationModel.getTitle());
                notificationIntent.putExtra("isMessenger", true);
                notificationIntent.putExtra("package_name", context.getPackageName());
                if (ChannelizeMainActivity.IS_RECENT_CHAT_VISIBLE) {
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                } else {
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), notificationIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                String message = "";
                for (String msg : pushNotificationModel.getMsgList()) {
                    totalMessageCount++;
                    inboxStyle.addLine(msg);
                    message = msg;
                }

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationBuilder.setChannelId(CHANNEL_ID);
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            pushNotificationModel.getTitle(),
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    if (MyFcmListenerService.messengerNotificationsManager != null) {
                        MyFcmListenerService.messengerNotificationsManager.createNotificationChannel(channel);
                    }
                }

                Notification notification = notificationBuilder.setSmallIcon(R.drawable.push_noti_icon)
                        .setContentTitle(pushNotificationModel.getTitle())
                        .setContentText(message)
                        .setGroup(GROUP_KEY)
                        .setColor(ContextCompat.getColor(context, R.color.themeButtonColor))
                        .setStyle(inboxStyle)
                        .setContentIntent(pendingIntent)
                        .build();
                MyFcmListenerService.NOTIFICATION_MAP.put(chatMessages.getKey(), notification);
            }
        }

        Intent intent = new Intent(context, ChannelizeMainActivity.class);
        if (ChannelizeMainActivity.IS_RECENT_CHAT_VISIBLE) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        intent.putExtra(Constants.IS_REDIRECTED_FROM_PUSH_NOTIFICATION, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String summaryText = getSummaryText(context, totalMessageCount);
        inboxStyle.setSummaryText(summaryText);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(CHANNEL_ID);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (MyFcmListenerService.messengerNotificationsManager != null) {
                MyFcmListenerService.messengerNotificationsManager.createNotificationChannel(channel);
            }
        }

        Notification summaryNotification = mBuilder.setContentTitle(context.getResources().getString(R.string.app_name))
                //set content text to support devices running API level < 24
                .setSmallIcon(R.drawable.push_noti_icon)
                //build summary info into InboxStyle template
                .setStyle(inboxStyle)
                //specify which group this notification belongs to
                .setGroup(GROUP_KEY)
                .setContentText(summaryText)
                .setColor(ContextCompat.getColor(context, R.color.themeButtonColor))
                //set this notification as the summary for the group
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .build();

        if (!MyFcmListenerService.NOTIFICATION_MAP.isEmpty()) {
            int counter = 1;
            for (Map.Entry<String, Notification> notificationEntry : MyFcmListenerService.NOTIFICATION_MAP.entrySet()) {
                PushNotificationModel pushNotificationModel = MyFcmListenerService.NOTIFICATION_CHATS_MAP.get(notificationEntry.getKey());
                pushNotificationModel.setNotificationId(counter);
                MyFcmListenerService.messengerNotificationsManager.notify(counter, notificationEntry.getValue());
                counter++;
            }
            MyFcmListenerService.messengerNotificationsManager.notify(SUMMARY_ID, summaryNotification);
        } else {
            MyFcmListenerService.clearMessengerPushNotification();
        }
    }

    private static String getSummaryText(Context context, int totalMessageCount) {
        String messageText = context.getResources().getQuantityString(R.plurals.pm_message_count,
                totalMessageCount, totalMessageCount);
        int chatCount = MyFcmListenerService.NOTIFICATION_CHATS_MAP.size();
        String chatText = context.getResources().getQuantityString(R.plurals.pm_chat_count,
                chatCount, chatCount);

        return messageText + " " + chatText;
    }

}
