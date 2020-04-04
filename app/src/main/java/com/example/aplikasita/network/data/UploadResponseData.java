package com.example.aplikasita.network.data;

import com.google.gson.annotations.SerializedName;

import java.io.File;

public class UploadResponseData {
    @SerializedName("file_url")
    private String fileUrl;

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
