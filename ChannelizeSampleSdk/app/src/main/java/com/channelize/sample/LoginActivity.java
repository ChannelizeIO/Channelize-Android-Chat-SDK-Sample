/*
 *   Copyright (c) 2018 BigStep Technologies Private Limited.
 *
 *   The distribution of this source code is prohibited.
 */

package com.channelize.sample;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import java.io.IOException;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // Member variables.
    private Context mContext;
    private TextInputLayout ilEmail, ilPassword;
    private TextInputEditText etEmail, etPassword;
    private LinearLayout login_button_layout;
    private Button btnLogin, btnLogout;
    private ProgressDialog progressDialog;
    private Channelize channelize;
    private ChannelizeUtils channelizeUtils;
    private TextView signupText;
    private static final String CHANNEL_ID = "pm_channel_123";
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);

        mContext = this;
        channelize = Channelize.getInstance();
        String currentUserId = ChannelizePreferences.getCurrentUserId(mContext);
        channelizeUtils = ChannelizeUtils.getInstance();
        if (currentUserId != null && !currentUserId.isEmpty()
                && !currentUserId.equals("null")) {
            channelize.setCurrentUserId(currentUserId);
            Channelize.getInstance().setCurrentUserId(currentUserId);
        }
        channelizeUtils.setAppActiveStatus(true);
        initViews();
        invalidateOptionsMenu();

        Logcat.d(LoginActivity.class, "Inside Login Activity");
        if (getIntent().getExtras() != null) {
            Logcat.d(LoginActivity.class.getSimpleName(), "GetIntent not null");
            Bundle chatInfo = getIntent().getExtras();
            startMainActivity(chatInfo);
        } else {
            startMainActivity(null);
        }
    }

    private void initViews() {
        ilEmail = findViewById(R.id.emailWrapper);
        ilPassword = findViewById(R.id.passwordWrapper);
        etEmail = findViewById(R.id.email_field);
        etPassword = findViewById(R.id.password_field);
        btnLogin = findViewById(R.id.login_button);
        btnLogout = findViewById(R.id.logout_button);
        signupText = findViewById(R.id.signupText);
        login_button_layout = findViewById(R.id.login_button_layout);

        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        signupText.setOnClickListener(this);

        setViewsVisibility(channelize.getCurrentUserId() != null
                && !channelize.getCurrentUserId().isEmpty());

        progressDialog = new ProgressDialog(mContext,R.style.AppCompatAlertDialogStyle);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    private void setViewsVisibility(boolean isLoggedInUser) {
        ilEmail.setVisibility(isLoggedInUser ? View.GONE : View.VISIBLE);
        ilPassword.setVisibility(isLoggedInUser ? View.GONE : View.VISIBLE);
        btnLogin.setVisibility(isLoggedInUser ? View.GONE : View.VISIBLE);
        login_button_layout.setVisibility(isLoggedInUser ? View.GONE : View.VISIBLE);
        signupText.setVisibility(isLoggedInUser ? View.GONE : View.VISIBLE);
        btnLogout.setVisibility(isLoggedInUser ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mContext != null) {
            setViewsVisibility(channelize.getCurrentUserId() != null
                    && !channelize.getCurrentUserId().isEmpty());
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        channelizeUtils.setAppActiveStatus(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_messenger).setVisible(channelize.getCurrentUserId() != null
                && !channelize.getCurrentUserId().isEmpty());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_messenger) {
            startMainActivity(null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signupText:
                etEmail.setText("");
                etPassword.setText("");
                Intent intent=new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                break;

            case R.id.login_button:
                if (checkFields()) {
                    hideKeyboard();
                    progressDialog.setMessage("Logging-In ...");
                    progressDialog.show();
                    channelize.loginWithEmailPassword(etEmail.getText().toString(), "123456", new CompletionHandler<LoginResponse>() {
                        @Override
                        public void onComplete(LoginResponse result, ChannelizeError error) {
                            progressDialog.dismiss();
                            if (result != null && result.getUser() != null) {
                                startMainActivity(null);
                            } else if (error != null) {
                                SnackbarUtils.displaySnackbar(btnLogin, error.getMessage());
                            }
                        }
                    });
                }
                break;

            case R.id.logout_button:
                new Logout().execute();
                break;
        }

    }

    /**
     * Method to hide keyboard.
     */
    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void startMainActivity(Bundle chatInfo) {
        if (channelize.getCurrentUserId() != null
                && !channelize.getCurrentUserId().isEmpty()) {
            new FirebaseToken(true).execute();
            Channelize.connect();
            Logcat.d(LoginActivity.class, "Date Diff: " + channelizeUtils.getDateDiff());
            if (channelizeUtils.getDateDiff() == 15
                    || channelizeUtils.getDateDiff() == -1) {
                channelizeUtils.clearLastCache();
            }
            Intent intent = new Intent(mContext, ChannelizeMainActivity.class);
            intent.putExtra("package_name", mContext.getPackageName());
            if (chatInfo != null) {
                intent.putExtras(chatInfo);
            }
            startActivity(intent);
        }
    }

    protected boolean checkFields() {
        if (etEmail.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter Email Address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!etEmail.getText().toString().trim().matches(emailPattern)){
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
                setViewsVisibility(false);
                etEmail.setText("");
                etPassword.setText("");
                channelize.setCurrentUserId("");
                invalidateOptionsMenu();
            }
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
                    Logcat.d(LoginActivity.class, "Sender Id: " + mContext.getResources().getString(R.string.gcm_defaultSenderId));
                    return FirebaseInstanceId.getInstance().getToken(mContext.getResources().getString(R.string.gcm_defaultSenderId), "FCM");

                } else {
                    FirebaseInstanceId.getInstance().deleteToken(mContext.getResources().getString(R.string.gcm_defaultSenderId), "FCM");
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
            Logcat.d(LoginActivity.class, "Firebase Token: " + token);
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
        Logcat.d(LoginActivity.class, "SummaryText: " + summaryText);
        inboxStyle.setSummaryText(summaryText);

        Intent notificationIntent;
        if (ChannelizeMainActivity.IS_RECENT_CHAT_VISIBLE) {
            notificationIntent = new Intent(context, ChannelizeMainActivity.class);
        } else {
            notificationIntent = new Intent(context, LoginActivity.class);
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
                    notificationIntent = new Intent(context, LoginActivity.class);
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
