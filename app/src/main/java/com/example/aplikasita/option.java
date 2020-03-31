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
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import it.chengdazhi.styleimageview.StyleImageView;

import com.example.aplikasita.dehaze.ImageDehazeResult;
import com.example.aplikasita.storage.FilterStorage;
import com.example.dehaze.GuidedFilter;
import com.example.dehaze.HazeRemover;

public class option extends AppCompatActivity {

    private StyleImageView imageView;
    private StyleImageView imageViewProcess1;
    private StyleImageView imageViewProcess2;

    // FIXME Diwang nambahin bitmap khusus buat gambar aslinya
    private Bitmap originalBitmap;
    private BitmapDrawable originalBitmapDrawable;
    private FilterStorage newFilter;

    private SeekBar seekbarDehaze, seekBarBright, seekBarContrast, seekBarSaturation;
    private Uri imageUri;
    private EditText brightnessTxt, contrastTxt, saturationTxt, dehazeTxt;

    private Button savePhoto, saveFilter, dehazeButton, depthMap, histeqBtn;
    OutputStream outputStream;
//    private Random random = new Random();
//    private int sourceId;

    private final HazeRemover hazeRemover = new HazeRemover(new GuidedFilter(), 1500, 1500);
    private ImageDehazeResult downScaleDehazeResult;
    private ImageDehazeResult originalDehazeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        newFilter = new FilterStorage();

        // main image (ditampilin)
        // Menampilkan Imageview2 dari ImageView1
        Intent intent = getIntent();
        final Bitmap bitmap = intent.getParcelableExtra("image");

        imageView = findViewById(R.id.imageView2);
        imageViewProcess1 = findViewById(R.id.imageView2);
        imageViewProcess2 = findViewById(R.id.imageView2);

        if (bitmap != null) {
            originalBitmap = bitmap;
            imageView.setImageBitmap(bitmap);
        }

        // get bitmap

        seekbarDehaze = findViewById(R.id.seekbar_dehaze);
        seekBarBright = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        seekBarSaturation = findViewById(R.id.seekbar_saturation);

        brightnessTxt = findViewById(R.id.valueTxt1);
        contrastTxt = findViewById(R.id.valueTxt2);
        saturationTxt = findViewById(R.id.valueTxt3);
        dehazeTxt = findViewById(R.id.valueTxt4);


        savePhoto = findViewById(R.id.save_photo);
        saveFilter = findViewById(R.id.save_filter);
        dehazeButton = findViewById(R.id.dehaze_button);
        depthMap = findViewById(R.id.depthMap);
        histeqBtn = findViewById(R.id.histeqButton);
//        histeq2Btn = findViewById(R.id.histeq2Button);

        // Buat save Foto
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
                Bundle isiFilter = new Bundle();
                intent.putExtra("dehazeLevel",newFilter.getDehazeLevel());
                intent.putExtra("brightLevel",newFilter.getBrightLevel());
                intent.putExtra("contrastLevel", newFilter.getContrastLevel());
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


                // nge dehaze, terus tampilin imageview dehazed yang baru
                ImageDehazeResult[] resultDehazed = removeHazeOnBitmap(originalBitmap, 100);
                imageViewProcess1.setImageBitmap(resultDehazed[0].getResult());
                imageViewProcess2.setImageBitmap(resultDehazed[1].getResult());
                imageView.setImageBitmap(resultDehazed[2].getResult());
            }
        });

        //Button depthMap ( Untuk melihat mana kabut mana engga )
        depthMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                // nampilin depth map
                ImageDehazeResult[] resultDehazed = removeHazeOnBitmap(originalBitmap, 100);
                imageViewProcess1.setImageBitmap(resultDehazed[0].getDepth());
                imageViewProcess2.setImageBitmap(resultDehazed[1].getDepth());
                imageView.setImageBitmap(resultDehazed[2].getDepth());
            }
        });

        // Histogram Equalization Button
        histeqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mat sourceMat = new Mat();
                Utils.bitmapToMat(bitmap, sourceMat);
                Mat destinationMat = new Mat(sourceMat.size(), sourceMat.type());
                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_GRAY2RGB);
                Imgproc.equalizeHist(sourceMat, destinationMat);

                Bitmap equalizerBitmap = Bitmap.createBitmap(sourceMat.cols(), sourceMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(destinationMat, equalizerBitmap);

                imageView.setImageBitmap(equalizerBitmap);

            }
        });

//        //Histeq 2 Button
//        histeq2Btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Mat sourceMat = new Mat();
//                Utils.bitmapToMat(bitmap, sourceMat);
//                Mat destinationMat = new Mat(sourceMat.size(), sourceMat.type());
//                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_GRAY2RGB);
//                Imgproc.equalizeHist(sourceMat, destinationMat);
//
//                Bitmap equalizerBitmap = Bitmap.createBitmap(sourceMat.cols(), sourceMat.rows(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(destinationMat, equalizerBitmap);
//
//                imageView.setImageBitmap(equalizerBitmap);
//            }
//        });


        // Result Processing
//        class ResultOfProcessing {
//            private ImageDehazeResult originalResult;
//
//            public ResultOfProcessing(ImageDehazeResult originalResult) {
//                this.setOriginalResult(originalResult);
//
//            }
//
//            public ImageDehazeResult getOriginalResult() {
//                return originalResult;
//            }
//
//            public void setOriginalResult(ImageDehazeResult originalResult) {
//                this.originalResult = originalResult;
//            }
//
//        }

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

        dehazeTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // Menampilkan Brightness, Contrast, Saturation, dehaze dan edit value text
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
                        ImageDehazeResult[] resultDehazed = removeHazeOnBitmap(originalBitmap, progress);
                        imageViewProcess1.setImageBitmap(resultDehazed[0].getResult());
                        imageViewProcess2.setImageBitmap(resultDehazed[1].getResult());
                        imageView.setImageBitmap(resultDehazed[2].getResult());

                        // simpen nilai dari progress
                        newFilter.setDehazeLevel(progress);
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
                        newFilter.setBrightLevel(i);
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
    private ImageDehazeResult[] removeHazeOnBitmap(Bitmap src, int value) {

        // Objek yang berisi 3 buah hasil tiap proses
        ImageDehazeResult[] results = new  ImageDehazeResult[3];

        // ngambil pixel untuk parameter library si hazeremover
        int[] pixels = new int[src.getWidth() * src.getHeight()];
        src.getPixels(pixels, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

        // convert integer dari seekbar ke float untuk parameter library si hazeremover
        float threshold = getThreshold(value);

        // return bitmap hasil dehaze pake lib nya..
        results[0] = new ImageDehazeResult(hazeRemover.dehazeProcess1(pixels, src.getHeight(), src.getWidth(), threshold));
        results[1] = new ImageDehazeResult(hazeRemover.dehazeProcess2(pixels, src.getHeight(), src.getWidth(), threshold));
        results[2] = new ImageDehazeResult(hazeRemover.dehaze(pixels, src.getHeight(), src.getWidth(), threshold));


//        return new ImageDehazeResult(hazeRemover.dehaze(pixels, src.getHeight(), src.getWidth(), threshold));
        return results;
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
