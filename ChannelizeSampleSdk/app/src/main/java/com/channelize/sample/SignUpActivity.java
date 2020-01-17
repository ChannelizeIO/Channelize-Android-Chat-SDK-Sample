package com.channelize.sample;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.channelize.sample.Retrofit.ApiClient;
import com.channelize.sample.Retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText displayname_field, email_field, password_field;
    private Button signup_button;
    private TextView loginmessage;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private ProgressDialog progressDialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mContext=this;
        initView();

        loginmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (displayname_field.getText().toString() != null
                        && !displayname_field.getText().toString().isEmpty()
                        && email_field.getText().toString() != null
                        && !email_field.getText().toString().isEmpty()
                        && password_field.getText().toString() != null && !password_field.getText().toString().isEmpty()) {

                    if (checkEmail()) {
                        hitApiToSignUp();
                    }


                } else {
                    Toast.makeText(SignUpActivity.this, "Please enter all the fields", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private boolean checkEmail() {
        if (!email_field.getText().toString().trim().matches(emailPattern)) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    private void initView() {
        displayname_field = findViewById(R.id.displayname_field);
        email_field = findViewById(R.id.email_field);
        password_field = findViewById(R.id.password_field);
        signup_button = findViewById(R.id.signup_button);
        loginmessage = findViewById(R.id.loginmessage);

        progressDialog = new ProgressDialog(mContext,R.style.AppCompatAlertDialogStyle);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    private void hitApiToSignUp() {
        progressDialog.setMessage("Signing-Up ...");
        progressDialog.show();

        if (getBase64EncodedString() != null && !getBase64EncodedString().isEmpty()) {

            JsonObject jsonObject=new JsonObject();
            jsonObject.addProperty("displayName",displayname_field.getText().toString());
            jsonObject.addProperty("email",email_field.getText().toString());
            jsonObject.addProperty("password","123456");

            Call<ResponseBody> call = ApiClient.getClient().create(ApiInterface.class).doSignUp(getBase64EncodedString(),"application/json", jsonObject);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.e("Response", response.toString());
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Please enter credentials to login", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Please try again", Toast.LENGTH_LONG).show();
                        displayname_field.setText("");
                        email_field.setText("");
                        password_field.setText("");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Response", t.toString());
                    progressDialog.dismiss();
                }
            });
        }
    }

    private String getBase64EncodedString() {
        String privateKey = Config.API_PRIVATE_KEY;
        if (privateKey != null && !privateKey.isEmpty()) {
            String base64EncodedAccessToken = null;
            try {
                base64EncodedAccessToken = Base64.encodeToString(privateKey
                        .getBytes("UTF-8"), Base64.NO_WRAP);
                return "Basic " + base64EncodedAccessToken;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
