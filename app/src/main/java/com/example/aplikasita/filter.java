package com.example.aplikasita;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class filter extends AppCompatActivity {

    private TextView namaFilter1, namaFilter2, namaFilter3, namaFilter4, namaFilter5;
    private Button clear, rename, save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter2);
        namaFilter1 = findViewById(R.id.NamaFilter1);
        namaFilter2 = findViewById(R.id.NamaFilter2);
        namaFilter3 = findViewById(R.id.NamaFilter3);
        namaFilter4 = findViewById(R.id.NamaFilter4);
        namaFilter5 = findViewById(R.id.NamaFilter5);

        clear = findViewById(R.id.clcButton);
        rename = findViewById(R.id.renameButton);
        save = findViewById(R.id.saveBtn);


        // clear filter filter yang udah ke isi
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // rename nama filter yang udah ke isi
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // save untuk ada update perubahan
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

//        namaFilter1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        context = filter.this;
        Intent intent = getIntent();
        int dehaze; int bright; int contrast; int saturation;
        dehaze = intent.getIntExtra("dehazeLevel", 0);
        bright = intent.getIntExtra("brightLevel", 0);
        contrast =  intent.getIntExtra("contrastLevel", 0);
        saturation =  intent.getIntExtra("saturationLevel", 0);

        // ambil filter 1
        // Dehaze
        filter1.edit().putInt("dehazeLevel", dehaze).apply();
        // Brightness
        filter1.edit().putInt("brightLevel", bright).apply();
        // Contrast
        filter1.edit().putInt("contrastLevel", contrast).apply();
        // Saturation
        filter1.edit().putInt("saturationLevel", saturation).apply();
        filter1 = context.getSharedPreferences("namaFilter1", MODE_PRIVATE);
        String nama = filter1.getString("namaFilter1", "");

        // ambil filter 2
        // Dehaze
        filter2.edit().putInt("dehazeLevel", dehaze).apply();
        // Brightness
        filter2.edit().putInt("brightLevel", bright).apply();
        // Contrast
        filter2.edit().putInt("contrastLevel", contrast).apply();
        // Saturation
        filter2.edit().putInt("saturationLevel", saturation).apply();
        filter2 = context.getSharedPreferences("namaFilter2", MODE_PRIVATE);
        String nama2 = filter2.getString("namaFilter2", "");

        // ambil filter 3
        // Dehaze
        filter3.edit().putInt("dehazeLevel", dehaze).apply();
        // Brightness
        filter3.edit().putInt("brightLevel", bright).apply();
        // Contrast
        filter3.edit().putInt("contrastLevel", contrast).apply();
        // Saturation
        filter3.edit().putInt("saturationLevel", saturation).apply();
        filter3 = context.getSharedPreferences("namaFilter3", MODE_PRIVATE);
        String nama3 = filter3.getString("namaFilter3", "");

        // ambil filter 4
        // Dehaze
        filter4.edit().putInt("dehazeLevel", dehaze).apply();
        // Brightness
        filter4.edit().putInt("brightLevel", bright).apply();
        // Contrast
        filter4.edit().putInt("contrastLevel", contrast).apply();
        // Saturation
        filter4.edit().putInt("saturationLevel", saturation).apply();
        filter4 = context.getSharedPreferences("namaFilter2", MODE_PRIVATE);
        String nama4 = filter4.getString("namaFilter4", "");

        // ambil filter 5
        // Dehaze
        filter5.edit().putInt("dehazeLevel", dehaze).apply();
        // Brightness
        filter5.edit().putInt("brightLevel", bright).apply();
        // Contrast
        filter5.edit().putInt("contrastLevel", contrast).apply();
        // Saturation
        filter5.edit().putInt("saturationLevel", saturation).apply();
        filter5 = context.getSharedPreferences("namaFilter2", MODE_PRIVATE);
        String nama5 = filter5.getString("namaFilter5", "");


        // Filter 1
        filter1 = context.getSharedPreferences("dehazeLevel", MODE_PRIVATE);
        filter1 = context.getSharedPreferences("brightLevel", MODE_PRIVATE);
        filter1 = context.getSharedPreferences("contrastLevel", MODE_PRIVATE);
        filter1 = context.getSharedPreferences("saturationLevel", MODE_PRIVATE);

        //Filter 2
        filter2 = context.getSharedPreferences("dehazeLevel", MODE_PRIVATE);
        filter2 = context.getSharedPreferences("brightLevel", MODE_PRIVATE);
        filter2 = context.getSharedPreferences("contrastLevel", MODE_PRIVATE);
        filter2 = context.getSharedPreferences("saturationLevel", MODE_PRIVATE);

        //Filter 3
        filter3 = context.getSharedPreferences("dehazeLevel", MODE_PRIVATE);
        filter3 = context.getSharedPreferences("brightLevel", MODE_PRIVATE);
        filter3 = context.getSharedPreferences("contrastLevel", MODE_PRIVATE);
        filter3 = context.getSharedPreferences("saturationLevel", MODE_PRIVATE);

        //Filter 4
        filter4 = context.getSharedPreferences("dehazeLevel", MODE_PRIVATE);
        filter4 = context.getSharedPreferences("brightLevel", MODE_PRIVATE);
        filter4 = context.getSharedPreferences("contrastLevel", MODE_PRIVATE);
        filter4 = context.getSharedPreferences("saturationLevel", MODE_PRIVATE);

        //Filter 5
        filter5 = context.getSharedPreferences("dehazeLevel", MODE_PRIVATE);
        filter5 = context.getSharedPreferences("brightLevel", MODE_PRIVATE);
        filter5 = context.getSharedPreferences("contrastLevel", MODE_PRIVATE);
        filter5 = context.getSharedPreferences("saturationLevel", MODE_PRIVATE);

        // tinggall tambah nanti, sama button buat rename edit dll

        // bikim texview(simpen di filter) button clear, button rename

    }
    private Context context;

    SharedPreferences filter1 =  context.getSharedPreferences("filter1", MODE_PRIVATE);
    SharedPreferences filter2 =  context.getSharedPreferences("filter2", MODE_PRIVATE);
    SharedPreferences filter3 =  context.getSharedPreferences("filter3", MODE_PRIVATE);
    SharedPreferences filter4 =  context.getSharedPreferences("filter4", MODE_PRIVATE);
    SharedPreferences filter5 =  context.getSharedPreferences("filter5", MODE_PRIVATE);

}
