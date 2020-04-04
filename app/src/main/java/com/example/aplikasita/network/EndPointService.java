package com.example.aplikasita.network;

import com.example.aplikasita.network.data.UploadResponseData;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

// Membuat end point untuk menghubungkan server
public interface EndPointService {
    @Multipart
    @POST("/upload")
    Call<UploadResponseData> uploadData(@Part MultipartBody.Part file);
}
