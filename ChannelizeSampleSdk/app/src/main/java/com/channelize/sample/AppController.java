/*
 *   Copyright (c) 2018 BigStep Technologies Private Limited.
 *
 *   The distribution of this source code is prohibited.
 */

package com.channelize.sample;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.channelize.apisdk.Channelize;
import com.channelize.apisdk.ChannelizeConfig;
import com.channelize.apisdk.utils.ChannelizePreferences;
import com.channelize.uisdk.ChannelizeUI;
import com.channelize.uisdk.ChannelizeUIConfig;
import com.channelize.uisdk.interfaces.OnConversationClickListener;
import com.channelize.uisdk.utils.ChannelizeUtils;


public class AppController extends MultiDexApplication implements OnConversationClickListener {

    private static Context context;
    private Channelize channelize;

    public AppController() {
    }

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        initializeChannelize();
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());
    }

    /**
     * Method to initialize channelize components.
     */
    private void initializeChannelize() {
        ChannelizeConfig channelizeConfig = new ChannelizeConfig.Builder(this)
                .setAPIKey(Config.API_KEY)
                .setLoggingEnabled(true).build();
        Channelize.initialize(channelizeConfig);
        Channelize.getInstance().setCurrentUserId(ChannelizePreferences.getCurrentUserId(getContext()));
        if (Channelize.getInstance().getCurrentUserId() != null
                && !Channelize.getInstance().getCurrentUserId().isEmpty()) {
            Channelize.connect();
        }

        ChannelizeUIConfig channelizeUIConfig = new ChannelizeUIConfig.Builder()
                .enableCall(true)
                .build();
        ChannelizeUI.initialize(channelizeUIConfig);
        channelize = Channelize.getInstance();

        ChannelizeUtils channelizeUtils = ChannelizeUtils.getInstance();
        channelizeUtils.setOnConversationClickListener(this);
    }

    @Override
    public void onConversationOpen(String conversationId) {
        ChannelizeUI.getInstance().onConversationOpen(getApplicationContext(), conversationId);
    }

    class AppLifecycleTracker implements Application.ActivityLifecycleCallbacks {

        private int numStarted = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (numStarted == 0 && channelize != null) {
                // app went to foreground
                channelize.setUserOnline();
            }
            numStarted++;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            numStarted--;
            if (numStarted == 0 && channelize != null) {
                // app went to background
                channelize.setUserOffline();
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (numStarted == 0) {
                ChannelizeUtils.getInstance().appClosed(context);
            }
        }

    }
}
