package com.example.aplikasita;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
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
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;

import static org.opencv.core.Core.absdiff;
import static org.opencv.core.CvType.CV_32F;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Core;
import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.Core.BORDER_DEFAULT;

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
    private SeekBar seekbarDehaze2;
    private Uri imageUri;
    private EditText brightnessTxt, contrastTxt, saturationTxt, dehazeTxt;
    private EditText dehaze2Txt;

    private TextView MSEhsl, PSNRhsl;

    private Button savePhoto, saveFilter, dehazeButton, depthMap, histeqBtn;
    private Button PSNRbtn, MSEbtn;
    private Button dehaze2Button;
    OutputStream outputStream;
//    private Random random = new Random();
//    private int sourceId;

    private final HazeRemover hazeRemover = new HazeRemover(new GuidedFilter(), 1500, 1500);
    private int progressSeekbarDehaze;

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
        imageViewProcess1 = findViewById(R.id.imageView3);
        imageViewProcess2 = findViewById(R.id.imageView4);

        if (bitmap != null) {
            originalBitmap = bitmap;
            imageView.setImageBitmap(bitmap);
        }

        // get bitmap

        seekbarDehaze = findViewById(R.id.seekbar_dehaze);
        seekbarDehaze2 = findViewById(R.id.seekbar_dehaze2);
        seekBarBright = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        seekBarSaturation = findViewById(R.id.seekbar_saturation);

        brightnessTxt = findViewById(R.id.valueTxt1);
        contrastTxt = findViewById(R.id.valueTxt2);
        saturationTxt = findViewById(R.id.valueTxt3);
        dehazeTxt = findViewById(R.id.valueTxt4);
        dehaze2Txt = findViewById(R.id.valueTxt5);

        MSEhsl = findViewById(R.id.mseHsl);
        PSNRhsl = findViewById(R.id.psnrHsl);

        savePhoto = findViewById(R.id.save_photo);
        saveFilter = findViewById(R.id.save_filter);
        dehazeButton = findViewById(R.id.dehaze_button);
        dehaze2Button = findViewById(R.id.dehaze2_button);
        depthMap = findViewById(R.id.depthMap);
        histeqBtn = findViewById(R.id.histeqButton);
        PSNRbtn = findViewById(R.id.psnrBtn);
        MSEbtn = findViewById(R.id.mseBtn);
//        histeq2Btn = findViewById(R.id.histeq2Button);

        PSNRbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hasilpsnr = "";
                double psnr = getMSE(originalBitmap, imageView.getBitmap())[1];
                hasilpsnr = String.format("%.2f", psnr);
                PSNRhsl.setText(hasilpsnr);

            }
        });

        MSEbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hasilmse = "";
                double mse = getMSE(originalBitmap, imageView.getBitmap())[0];
                hasilmse = String.format("%.2f", mse);
                MSEhsl.setText(hasilmse);

            }
        });

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
                intent.putExtra("dehazeLevel", newFilter.getDehazeLevel());
                intent.putExtra("brightLevel", newFilter.getBrightLevel());
                intent.putExtra("contrastLevel", newFilter.getContrastLevel());
                intent.putExtra("saturationLevel", newFilter.getSatLevel());
                startActivity(intent);
            }
        });

        // Button Result Dehaze 2
        dehaze2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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



                // Cari filter lain, cari parameter nya apa bandingkan dengan cara kualitatif ( masih nyoba on progress )
                // Nilai kabutnya darimana (threshold) , cek dengan rumus nya kabut nya masih ada apa engga
                // Cara validasi kabut nya dari metode nya gimana ( depth map )

                // Validasi kabut ambil dari threshold transmission nya sama liat dari depth map

                // tulis di buku TA algoritma nyaa

                // process tiap gambar - dehaze gimana
                ImageDehazeResult[] resultDehazed = removeHazeOnBitmap(originalBitmap, 100);
                imageViewProcess1.setImageBitmap(resultDehazed[0].getResult());
                imageViewProcess2.setImageBitmap(resultDehazed[1].getResult());
                imageView.setImageBitmap(resultDehazed[2].getResult());
                getMatrik(bitmap);
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
//                imageViewProcess1.setImageBitmap(resultDehazed[0].getDepth());
//                imageViewProcess2.setImageBitmap(resultDehazed[1].getDepth());
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
//              Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_GRAY2RGB);
                Imgproc.equalizeHist(sourceMat, destinationMat);

                Bitmap equalizerBitmap = Bitmap.createBitmap(sourceMat.cols(), sourceMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(destinationMat, equalizerBitmap);

                imageView.setImageBitmap(equalizerBitmap);

            }
        });

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
        seekbarDehaze.setProgress(progressSeekbarDehaze);

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
                        imageView.setImageBitmap(resultDehazed[2].getResult());

                        progressSeekbarDehaze = progress;

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

        // FIXME Diwang nambahin satu line ini buat ngeset progressbar 'sesuai' sama level brightnessnya
        seekBarBright.setProgress(imageView.getBrightness() + 255);

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

        // FIXME ini juga
        seekBarContrast.setProgress((int) imageView.getContrast() * 100);

        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarContrast, int i, boolean fromUser) {
                imageView.setContrast(i / 100F).updateStyle();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                getMatrik(bitmap);
                contrastTxt.setText(String.valueOf(i - 250));
                newFilter.setContrastLevel(i);
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
                newFilter.setSatLevel(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarSaturation) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarSaturation) {

            }
        });
    }


    // Get Value PSNR dan MSE
    public double[] getMSE(Bitmap source, Bitmap result)
    {
        double[]hasil = new double[2];
        Mat resultMat = new Mat();
        Mat sourceMat = new Mat();
//        Log.e("hasill3", "mat" );
        Bitmap sourceBitmap = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap resultBitmap = result.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(sourceBitmap, sourceMat);
        Utils.bitmapToMat(resultBitmap, resultMat);
        Log.e("hasill4", "bitmap"+sourceMat);
        Mat s1 = new Mat();
        Core.absdiff(sourceMat, resultMat, s1);  // |I1 - I2|
        Log.e("hasillCore", "hasilll2" +s1);
        s1.convertTo(s1, CvType.CV_8U); // cannot make a square on 8 bits
        s1 = s1.mul(s1); // |I1 - I2|^2
        Log.e("yyoo", "hasilS1" +s1);

        Scalar s = Core.sumElems(s1); // sum element per channel


        double sse = s.val[0] + s.val[1] + s.val[2]; //sum channels
        Log.e("hai", "hasilSSE" +sse);
        if(sse <= 1e-30){ // for small values return zero
            Log.e("error", "smallvalues" );
            return new double[2];
        }

        // cari paper keluaran hasil psnr harus 30db
        // kenapa harus mse atau rmse (beda nya apa)
        // rmse di akarin


        else
        {
            double mse  = sse / (double)(sourceMat.channels() * sourceMat.total());
            double psnr = 10.0 * Math.log10((255 * 255) / mse);
            hasil[0] = mse;
            hasil[1] = psnr;
            Log.e("berhasil", "yeey");
            return hasil;
//            return new double[mse, psnr];

            //

        }
    }

    // Menampilkan Matrix Ketika gambar udanh di enhancement ( Contrast, Saturation, Brightness )
    // Dibuat 10 baris aja biar tidak nge lama load nya
    private void getMatrik(Bitmap imageBitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Log.d("Matrik", Arrays.toString(mat.get(mat.rows(), mat.cols())));
        for (int a = 0; a <(10); a++) {
            for (int b = 0; b <(10); b++) {
                Log.d("Matrik", "[" + a + "]" + "[" + b + "]" + Arrays.toString(mat.get(a, b)));
            }
        }
    }

    // FIXME Diwang nambahin method dehaze untuk ngubah bitmap jadi dehazed
    private ImageDehazeResult[] removeHazeOnBitmap(Bitmap src, int value) {

        // Objek yang berisi 3 buah hasil tiap proses
        ImageDehazeResult[] results = new ImageDehazeResult[3];

        // ngambil pixel untuk parameter library si hazeremover
        int[] pixels = new int[src.getWidth() * src.getHeight()];
        src.getPixels(pixels, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

        // convert integer dari seekbar ke float untuk parameter library si hazeremover
        float threshold = getThreshold(value);

        // Nilai Variable untuk dehaze di seekbar gimana ??
        // nilai tidak cuman satu
        // nilai masuk kemana di programnya mana

        // nilai hanya threshold nya saja untuk nge hapus dehaze nya


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

//        thresHold = 0.2f;
//
        return thresHold;
    }
}
