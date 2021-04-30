package com.cuelogic.android.nfc.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebApi {

    @POST("quick-assign?scope=23250")
    Call<Object> assignDevice(@Header("Authorization") String id, @Body RequestInfo info);
}


