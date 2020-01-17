package com.channelize.sample.Retrofit;


import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiInterface {

    /*@POST("users")
    Call<RequestBody> doSignUp(@Header("Authorization") String authorization,
                               @Header("content-Type") String contenttype,
                               @Field("displayName") String displayName,
                               @Field("email") String emailid,
                               @Field("password") String password);*/

    @POST("users")
    Call<ResponseBody> doSignUp(@Header("Authorization") String authorization,
                                @Header("content-Type") String contenttype,
                                @Body JsonObject jsonObject);


}
