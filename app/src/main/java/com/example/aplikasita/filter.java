package com.example.aplikasita;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class filter extends AppCompatActivity {

    private TextView namaFilter1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter2);
        namaFilter1 = findViewById(R.id.NamaFilter1);
        namaFilter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        context = filter.this;
        Intent intent = getIntent();
        int dehaze; int bright; int contrast; int saturation;
        dehaze = intent.getIntExtra("dehazeLevel", 0);
        bright = intent.getIntExtra("brightLevel", 0);
        contrast =  intent.getIntExtra("contrastLevell", 0);
        saturation =  intent.getIntExtra("saturationLevel", 0);

        filter1.edit().putInt("dehazeLevel", dehaze).apply();
        filter1 = context.getSharedPreferences("namaFilter1", MODE_PRIVATE);


        String nama = filter1.getString("namaFilter1", "");


        //

        filter1 = context.getSharedPreferences("dehazeLevel", MODE_PRIVATE);
        filter1 = context.getSharedPreferences("brightLevel", MODE_PRIVATE);
        filter1 = context.getSharedPreferences("contrastLevel", MODE_PRIVATE);
        filter1 = context.getSharedPreferences("saturationLevel", MODE_PRIVATE);

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
