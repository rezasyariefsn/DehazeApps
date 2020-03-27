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

import com.example.aplikasita.dehaze.ImageDehazeResult;
import com.example.dehaze.GuidedFilter;
import com.example.dehaze.HazeRemover;

public class option extends AppCompatActivity {

    private StyleImageView imageView;

    // FIXME Diwang nambahin bitmap khusus buat gambar aslinya
    private Bitmap originalBitmap;
    private BitmapDrawable originalBitmapDrawable;

    private SeekBar seekbarDehaze, seekBarBright, seekBarContrast, seekBarSaturation;
    private Uri imageUri;
    private EditText brightnessTxt, contrastTxt, saturationTxt;

    private Button savePhoto, saveFilter, dehazeButton;
    OutputStream outputStream;
    private Random random = new Random();
    private int sourceId;

    private final HazeRemover hazeRemover = new HazeRemover(new GuidedFilter(), 1500, 1500);
    private ImageDehazeResult downScaleDehazeResult;
    private ImageDehazeResult originalDehazeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);


        imageView = findViewById(R.id.imageView2);

        seekbarDehaze = findViewById(R.id.seekbar_dehaze);
        seekBarBright = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        seekBarSaturation = findViewById(R.id.seekbar_saturation);

        brightnessTxt = findViewById(R.id.valueTxt1);
        contrastTxt = findViewById(R.id.valueTxt2);
        saturationTxt = findViewById(R.id.valueTxt3);

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
                File Dir = new File(filepath.getAbsolutePath() + "/Demo/");
                Dir.mkdir();
                File file = new File(Dir, System.currentTimeMillis() + ".jpg");
                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Toast.makeText(getApplicationContext(), "Image Save Into Gallery", Toast.LENGTH_SHORT).show();
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
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
                // FIXME Diwang nambahin action waktu dehaze

                // ngambil bitmap dari picture yang ditampilin
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                originalBitmap = bitmap;

                // nge dehaze, terus tampilin imageview dehazed yang baru
                ImageDehazeResult resultDehazed = removeHazeOnBitmap(bitmap, 100);
                imageView.setImageBitmap(resultDehazed.getResult());
            }
        });

        // Result Processing
        class ResultOfProcessing {
            private ImageDehazeResult originalResult;

            public ResultOfProcessing(ImageDehazeResult originalResult) {
                this.setOriginalResult(originalResult);

            }

            public ImageDehazeResult getOriginalResult() {
                return originalResult;
            }

            public void setOriginalResult(ImageDehazeResult originalResult) {
                this.originalResult = originalResult;
            }

        }

//        private ImageDehazeResult dehaze(Bitmap) {
//
//        }

        // Membuat Value Text 1 -3
        brightnessTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0 && !s.toString().equals("-") && Integer.parseInt(s.toString()) < 255 && Integer.parseInt(s.toString()) > -255) {
                    imageView.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    getMatrik(bitmap);
                }
            }
        });

        contrastTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0 && !s.toString().equals("-") && Integer.parseInt(s.toString()) < 255 && Integer.parseInt(s.toString()) > -255) {
                    imageView.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    getMatrik(bitmap);
                }
            }
        });

        saturationTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0 && !s.toString().equals("-") && Integer.parseInt(s.toString()) < 255 && Integer.parseInt(s.toString()) > -255) {
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
        if (bitmap != null) {

            imageView.setImageBitmap(bitmap);
        }

        //FIXME Diwang nambahin seekbarDehaze
        seekbarDehaze.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // dimasukkan ke dalam Thread UI supaya bisa multithreading
                        // ngambil bitmap dari picture yang ditampilin

                        // nge dehaze, terus tampilin imageview dehazed yang baru
                        ImageDehazeResult resultDehazed = removeHazeOnBitmap(originalBitmap, progress);
                        imageView.setImageBitmap(resultDehazed.getResult());
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Menampilkan Brightness, Contrast, Saturation dan edit value text
        seekBarBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarBright, final int i, boolean fromUser) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setBrightness(i - 255).updateStyle();
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        getMatrik(bitmap);
                        brightnessTxt.setText(String.valueOf(i - 250));
                    }
                });

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
                contrastTxt.setText(String.valueOf(i - 250));
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
                saturationTxt.setText(String.valueOf(i - 250));
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
    private void getMatrik(Bitmap imageBitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        Log.d("Matrik", Arrays.toString(mat.get(mat.rows(), mat.cols())));

        // FIXME Diwang komen dibawah ini semua
//        for (int a=0 ; a<mat.rows();a++){
//            for (int b=0 ; b<mat.cols();b++){
//                Log.d("Matrik", "["+a+"]"+"["+b+"]"+Arrays.toString(mat.get(a, b)));
//            }
//        }
    }

    // FIXME Diwang nambahin method dehaze untuk ngubah bitmap jadi dehazed
    private ImageDehazeResult removeHazeOnBitmap(Bitmap src, int value) {

        // ngambil pixel untuk parameter library si hazeremover
        int[] pixels = new int[src.getWidth() * src.getHeight()];
        src.getPixels(pixels, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

        // convert integer dari seekbar ke float untuk parameter library si hazeremover
        float threshold = getThreshold(value);

        // return bitmap hasil dehaze pake lib nya..
        return new ImageDehazeResult(hazeRemover.dehaze(pixels, src.getHeight(), src.getWidth(), threshold));
    }

    // FIXME Diwang nambahin method ngubah int Seekbar jadi float
    private float getThreshold(int valueSeekbar) {
        float thresHold;
        // integer si seekbar itu 0 - 100, mesti di convert ke float yang grafiknya exponential (liat di paper)
        // 0 -> berarti nilai result besar, gambar semakin berkabut
        // 100 -> berarti nilai result kecil bgt, gambar semakin jelas
        float percent = (float) (valueSeekbar / 100);
        float decay = 1 - percent;

        thresHold = (float) Math.pow(decay, 1000);

        return thresHold;
    }
}
