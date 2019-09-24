package com.channelize.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.channelize.uisdk.ChannelizeMainActivity;
import com.channelize.uisdk.utils.ChannelizeUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;


public class MainActivity extends AppCompatActivity{

    private Context mContext;
    private ProgressDialog progressDialog;
    private Channelize channelize;
    private ChannelizeUtils channelizeUtils;
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

}
