package com.example.aplikasita.network.data;

import com.example.aplikasita.network.EndPointService;
import com.example.aplikasita.network.RetrofitBaseService;

public class RetrofitService {
    public static EndPointService endPointService() {
        return RetrofitBaseService.getApiClient().create(EndPointService.class);
    }
}
