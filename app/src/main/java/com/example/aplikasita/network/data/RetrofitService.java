package com.example.aplikasita.network.data;

import com.example.aplikasita.network.EndPointService;
import com.example.aplikasita.network.RetrofitBaseService;

// Untuk Create endpoint service agar bisa di gabungin ke retrobaseservice
public class RetrofitService {
    public static EndPointService endPointService() {
        return RetrofitBaseService.getApiClient().create(EndPointService.class);
    }
}
