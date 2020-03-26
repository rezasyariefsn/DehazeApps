package com.example.aplikasita;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import it.chengdazhi.styleimageview.StyleImageView;
import com.example.dehaze.GuidedFilter;
import com.example.dehaze.HazeRemover;
import com.example.aplikasita.lib.Constant;
import com.example.aplikasita.lib.Toaster;
import com.example.aplikasita.lib.UriToUrl;

public class option extends AppCompatActivity {

    StyleImageView imageView;
    SeekBar seekBarBright, seekBarContrast, seekBarSaturation;
    Uri imageUri;
    EditText valueTxt1, valueTxt2, valueTxt3;


    private Button savePhoto, saveFilter, dehazeButton;
    OutputStream outputStream;
    private final HazeRemover hazeRemover = new HazeRemover(new GuidedFilter(), 1500, 1500);
//    private ImageDehazeResult downScaleDehazeResult;
    private ImageDehazeResult originalDehazeResult;
    private Random random = new Random();
    private int sourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        imageView = findViewById(R.id.imageView2);
        seekBarBright = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        seekBarSaturation = findViewById(R.id.seekbar_saturation);
        valueTxt1 = findViewById(R.id.valueTxt1);
        valueTxt2 = findViewById(R.id.valueTxt2);
        valueTxt3 = findViewById(R.id.valueTxt3);
        savePhoto = findViewById(R.id.save_photo);
        saveFilter = findViewById(R.id.save_filter);
        dehazeButton = findViewById(R.id.dehaze_button);

        // Butat save Foto
        savePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File filepath = Environment.getExternalStorageDirectory();
                File Dir = new File(filepath.getAbsolutePath()+"/Demo/");
                Dir.mkdir();
                File file = new File(Dir, System.currentTimeMillis()+".jpg");
                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Toast.makeText(getApplicationContext(), "Image Save Into Gallery", Toast.LENGTH_SHORT).show();
                try {
                    outputStream.flush();
                } catch (IOException e){
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        });

        // Button Save Filter ( Ke UI Save Filter )
        saveFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), filter.class);
                startActivity(intent);
            }
        });

        // Button Result Dehaze
        dehazeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Result Processing
        class ResultOfProcessing {
            private ImageDehazeResult originalResult;

            public ResultOfProcessing(ImageDehazeResult originalResult){
                this.setOriginalResult(originalResult);

            }
            public ImageDehazeResult getOriginalResult(){ return originalResult; }
            public void setOriginalResult(ImageDehazeResult originalResult){
                this.originalResult = originalResult;
            }

        }

//        private ImageDehazeResult dehaze(Bitmap) {
//
//        }

        // Membuat Value Text 1 -3
        valueTxt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0 && !s.toString().equals("-") && Integer.parseInt(s.toString()) < 255 && Integer.parseInt(s.toString()) > -255){
                    imageView.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    getMatrik(bitmap);
                }
            }
        });

        valueTxt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0 && !s.toString().equals("-") && Integer.parseInt(s.toString()) < 255 && Integer.parseInt(s.toString()) > -255){
                    imageView.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    getMatrik(bitmap);
                }
            }
        });

        valueTxt3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0 && !s.toString().equals("-") && Integer.parseInt(s.toString()) < 255 && Integer.parseInt(s.toString()) > -255){
                    imageView.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    getMatrik(bitmap);
                }
            }
        });

        // Menampilkan Imageview2 dari ImageView1
        Intent intent = getIntent();
        Bitmap bitmap = intent.getParcelableExtra("image");
        if(bitmap!=null){

            imageView.setImageBitmap(bitmap);
        }

        // Menampilkan Brightness, Contrast, Saturation dan edit value text
        seekBarBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarBright, int i, boolean fromUser) {
                imageView.setBrightness(i - 255).updateStyle();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                getMatrik(bitmap);
                valueTxt1.setText(String.valueOf(i-250));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarBright) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarBright) {

            }
        });

        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarContrast, int i, boolean fromUser) {
                imageView.setContrast(i / 100F).updateStyle();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                getMatrik(bitmap);
                valueTxt2.setText(String.valueOf(i-250));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarContrast) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarContrast) {

            }
        });

        seekBarSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarSaturation, int i, boolean fromUser) {
                imageView.setSaturation(i / 100F).updateStyle();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                getMatrik(bitmap);
                valueTxt3.setText(String.valueOf(i-250));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarSaturation) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarSaturation) {

            }
        });



//        if (getIntent().getExtras() !=null){
//            imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
//            imageView.setImageURI(imageUri);
//        }

//        else if (getIntent().getExtras() != null) {
//            imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
//            imageView.setImageURI(imageUri);
//        }

    }

    // Menampilkan Matrix Ketika gambar udanh di enhancement ( Contrast, Saturation, Brightness )
    private void getMatrik(Bitmap imageBitmap){
        Mat mat = new Mat();
        Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        Log.d("Matrik", Arrays.toString(mat.get(mat.rows(), mat.cols())));
        for (int a=0 ; a<mat.rows();a++){
            for (int b=0 ; b<mat.cols();b++){
                Log.d("Matrik", "["+a+"]"+"["+b+"]"+Arrays.toString(mat.get(a, b)));
            }
        }
    }
}
