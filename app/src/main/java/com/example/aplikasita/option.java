package com.example.aplikasita;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;

import static com.example.dehaze.HazeRemover.TRANSMISSION_THRESHOLD;
import static org.opencv.core.Core.absdiff;

import org.opencv.core.Core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import it.chengdazhi.styleimageview.StyleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.bumptech.glide.Glide;
import com.example.aplikasita.bitmap.BitmapLoader;
import com.example.aplikasita.dehaze.ImageDehazeResult;
import com.example.aplikasita.lib.UriToUrl;
import com.example.aplikasita.network.data.RetrofitService;
import com.example.aplikasita.network.data.UploadResponseData;
//import com.example.aplikasita.storage.FilterStorage;
import com.example.dehaze.GuidedFilter;
import com.example.dehaze.HazeRemover;

public class option extends AppCompatActivity {

    private StyleImageView imageView;
    private StyleImageView imageViewDehaze;
    private StyleImageView imageViewDepthMap;

    // FIXME Diwang nambahin bitmap khusus buat gambar aslinya
    private Bitmap originalBitmap;
    private BitmapDrawable originalBitmapDrawable;
//    private FilterStorage newFilter;
    private BitmapLoader bitmapLoader;
    private ProgressBar progressBar;

    private SeekBar seekbarDehaze, seekBarBright, seekBarContrast, seekBarSaturation;

    private Uri imageUri;
    private String imageUrl;
    private EditText brightnessTxt, contrastTxt, saturationTxt, dehazeTxt;

//    private TextView MSEhsl, PSNRhsl;

    private Button savePhoto, dehazeButton, depthMap, histeqBtn;
//    private Button PSNRbtn, MSEbtn;
    private Button dehaze2Button;
    private Button tmpl_gmbr;
    OutputStream outputStream;

    private final HazeRemover hazeRemover = new HazeRemover(new GuidedFilter(), 2000, 2000);
    private int progressSeekbarDehaze;
    private static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

//        newFilter = new FilterStorage();

        // main image (ditampilin)


        imageView = findViewById(R.id.imageView2);
        imageViewDehaze = findViewById(R.id.imageView3);
        imageViewDepthMap = findViewById(R.id.imageView4);
//        seekbarDehaze = findViewById(R.id.seekbar_dehaze);
        seekBarBright = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        seekBarSaturation = findViewById(R.id.seekbar_saturation);

        brightnessTxt = findViewById(R.id.valueTxt1);
        contrastTxt = findViewById(R.id.valueTxt2);
        saturationTxt = findViewById(R.id.valueTxt3);
        dehazeTxt = findViewById(R.id.valueTxt4);
//        dehaze2Txt = findViewById(R.id.valueTxt5);

//        MSEhsl = findViewById(R.id.mseHsl);
//        PSNRhsl = findViewById(R.id.psnrHsl);

        savePhoto = findViewById(R.id.save_photo);
//        saveFilter = findViewById(R.id.save_filter);
        dehazeButton = findViewById(R.id.dehaze_button);
        dehaze2Button = findViewById(R.id.dehaze2_button);
        depthMap = findViewById(R.id.depthMap);
        histeqBtn = findViewById(R.id.histeqButton);
//        PSNRbtn = findViewById(R.id.psnrBtn);
//        MSEbtn = findViewById(R.id.mseBtn);
        tmpl_gmbr = findViewById(R.id.tmplGambar_button);
//        histeq2Btn = findViewById(R.id.histeq2Button);

        // Menampilkan Imageview2 dari ImageView1
        // Diwang nambahin imageUri yang didapet dari potretan sebelumnya
        imageUri = getIntent().getData();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                OriginalImageLoaderThread oriImageLoader = new OriginalImageLoaderThread();
                oriImageLoader.execute();
            }
        });
//        OriginalImageLoaderThread oriImageLoader = new OriginalImageLoaderThread();
//        oriImageLoader.execute();
        tmpl_gmbr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalImageLoaderThread oriImageLoader = new OriginalImageLoaderThread();
                oriImageLoader.execute();
            }
        });

//        PSNRbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String hasilpsnr = "";
//                double psnr = getMSE(originalBitmap, imageViewDehaze.getBitmap())[1];
//                hasilpsnr = String.format("%.2f", psnr);
//                PSNRhsl.setText(hasilpsnr);
//
//            }
//        });

//        MSEbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String hasilmse = "";
//                double mse = getMSE(originalBitmap, imageViewDehaze.getBitmap())[0];
//                hasilmse = String.format("%.2f", mse);
//                MSEhsl.setText(hasilmse);
//
//            }
//        });

        // Buat save Foto
        savePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) imageViewDehaze.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                FileOutputStream outputStream = null;

                File sdCard = Environment.getExternalStorageDirectory();
                File directory = new File(sdCard.getAbsolutePath() + "/Demo/");
                directory.mkdir();
                String fileName = String.format("%d.jpg",System.currentTimeMillis());
                File outFile = new File(directory,fileName);

                Toast.makeText(option.this, "Image Save Into gallery", Toast.LENGTH_SHORT).show();

                try {
                    outputStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    Intent intent = new Intent((Intent.ACTION_MEDIA_SCANNER_SCAN_FILE));
                    intent.setData(Uri.fromFile(outFile));
                    sendBroadcast(intent);

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



//                File file = new File(Dir, System.currentTimeMillis() + ".jpg");
//                try {
//                    outputStream = new FileOutputStream(file);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                Toast.makeText(getApplicationContext(), "Image Save Into Gallery", Toast.LENGTH_SHORT).show();
//                try {
//                    outputStream.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
        });

        // Button Save Filter ( Ke UI Save Filter )
//        saveFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), filter.class);
//                Bundle isiFilter = new Bundle();
//                intent.putExtra("dehazeLevel", newFilter.getDehazeLevel());
//                intent.putExtra("brightLevel", newFilter.getBrightLevel());
//                intent.putExtra("contrastLevel", newFilter.getContrastLevel());
//                intent.putExtra("saturationLevel", newFilter.getSatLevel());
//                startActivity(intent);
//            }
//        });

        // Button Result Dehaze 2
        dehaze2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(originalBitmap);
            }
        });


        // Button Result Dehaze
        dehazeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nampilin Hasil Dehaze bedasarkan dehazedTxt

                // 1. ambil value di dalem txt, edit apa yg diinput user
                String valueInput = dehazeTxt.getText().toString().trim();

                // 2. set dulu hasil editan kita ke dehazedtxt nya
                dehazeTxt.setText(valueInput);

                // 3. validate input text
                if (isInputDehazeValueValid(valueInput)){

                    // 4. bikin loading bar
                    progressBar = findViewById(R.id.progressBar1);
                    progressBar.setVisibility(View.VISIBLE);

                    // 5. ambil nilai float dari value nya
                    float floatInputValue = Float.parseFloat(valueInput);

                    // 6. lakukan proses dehaze
                    DehazedImageLoaderThread loadImageHasilDehaze = new DehazedImageLoaderThread(floatInputValue);
                    loadImageHasilDehaze.execute(originalBitmap);
                } else {
                    Toast.makeText(getApplicationContext(),"Masukan angka yang valid!",Toast.LENGTH_SHORT).show();
                }
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
                Utils.bitmapToMat(originalBitmap, sourceMat);
                Mat destinationMat = new Mat(sourceMat.size(), sourceMat.type());
                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_RGB2GRAY);
//              Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_GRAY2RGB);
                Imgproc.equalizeHist(sourceMat, destinationMat);

                Bitmap equalizerBitmap = Bitmap.createBitmap(sourceMat.cols(), sourceMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(destinationMat, equalizerBitmap);

                imageViewDehaze.setImageBitmap(equalizerBitmap);

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
                    imageViewDehaze.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewDehaze.getDrawable();
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
                    imageViewDehaze.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewDehaze.getDrawable();
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
                    imageViewDehaze.setBrightness(Integer.parseInt(s.toString())).updateStyle();
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewDehaze.getDrawable();
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
                if (count>before){
                    if (s.subSequence(start, start + 1).toString().equalsIgnoreCase("\n")) {

                        Log.d("Option","after enter button");

                        String value = s.toString();
                        float thresholdValue = Float.parseFloat(value);

                        DehazedImageLoaderThread loadImageHasilDehaze = new DehazedImageLoaderThread(thresholdValue);
                        loadImageHasilDehaze.execute(originalBitmap);
                        // Tinggal ubah value threshold
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


        // Menampilkan Brightness, Contrast, Saturation, dehaze dan edit value text

//        //FIXME Diwang nambahin seekbarDehaze
//        seekbarDehaze.setProgress(progressSeekbarDehaze);
//
//        seekbarDehaze.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // dimasukkan ke dalam Thread UI supaya bisa multithreading
//                        // ngambil bitmap dari picture yang ditampilin
//
//                        // nge dehaze, terus tampilin imageview dehazed yang baru
//                        ImageDehazeResult[] resultDehazed = removeHazeOnBitmap(originalBitmap, progress);
//                        imageViewDehaze.setImageBitmap(resultDehazed[2].getResult());
//
//                        progressSeekbarDehaze = progress;
//
//                        // simpen nilai dari progress
//                        newFilter.setDehazeLevel(progress);
//                    }
//                });
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

        seekBarBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarBright, final int i, boolean fromUser) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageViewDehaze.setBrightness(i - 255).updateStyle();
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewDehaze.getDrawable();
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

        // FIXME ini juga
        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarContrast, int i, boolean fromUser) {
                imageViewDehaze.setContrast(i / 100F).updateStyle();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewDehaze.getDrawable();
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
                imageViewDehaze.setSaturation(i / 100F).updateStyle();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewDehaze.getDrawable();
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

    // FIXME ini method untuk validasi decimal input nya
    private boolean isInputDehazeValueValid(String valueInput) {
        Pattern decimalPattern = Pattern.compile("^\\d+(\\.\\d+)?$");
        return decimalPattern.matcher(valueInput).matches();
    }

    // Untuk Upload gambar ke server
    private void uploadFile(Bitmap bitmap) {
        // Nge buat filename dari bitmap
        // count di buat untuk membedakan nama file, di tambahkan static biar nilai nya beda
        String filename = "image"+(count++)+ ".jpg";
        File f = new File(this.getCacheDir(), filename);
        try {
            f.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Convert bitmap ke bentuk JPG
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //Bikin File dari bitmap untuk upload ke server
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ngirim file yang udah di buat ke server
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), f);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", f.getName(), reqFile);
        // ambil data dari server ketika sukses
        Call<UploadResponseData> call = RetrofitService.endPointService().uploadData(body);
        if (call != null) {
            call.enqueue(new Callback<UploadResponseData>() {
                @Override
                public void onResponse(Call<UploadResponseData> call, Response<UploadResponseData> response) {
                    if (response.isSuccessful()) {
                        String file_url = response.body().getFileUrl();
                        // taro di glide untuk ambil data nya
                        Glide.with(option.this).asBitmap().load(file_url).into(imageViewDehaze);
                        Log.e("glide", "hasilglide" + imageViewDehaze);
                    }
                }

                @Override
                public void onFailure(Call<UploadResponseData> call, Throwable t) {
                    Log.d("onFailure", t.getMessage());
                }
            });
        }
    }


    // Get Value PSNR dan MSE
    public double[] getMSE(Bitmap source, Bitmap result) {
        double[] hasil = new double[2];
        Mat resultMat = new Mat();
        Mat sourceMat = new Mat();
//        Log.e("hasill3", "mat" );
        Bitmap sourceBitmap = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap resultBitmap = result.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(sourceBitmap, sourceMat);
        Utils.bitmapToMat(resultBitmap, resultMat);
        Log.e("hasill4", "bitmap" + sourceMat);
        Mat s1 = new Mat();
        Core.absdiff(sourceMat, resultMat, s1);  // |I1 - I2|
        Log.e("hasillCore", "hasilll2" + s1);
        s1.convertTo(s1, CvType.CV_8U); // cannot make a square on 8 bits
        s1 = s1.mul(s1); // |I1 - I2|^2
        Log.e("yyoo", "hasilS1" + s1);

        Scalar s = Core.sumElems(s1); // sum element per channel


        double sse = s.val[0] + s.val[1] + s.val[2]; //sum channels
        Log.e("hai", "hasilSSE" + sse);
        if (sse <= 1e-30) { // for small values return zero
            Log.e("error", "smallvalues");
            return new double[2];
        }

        // cari paper keluaran hasil psnr harus 30db
        // kenapa harus mse atau rmse (beda nya apa)
        // rmse di akarin


        else {
            double mse = sse / (double) (sourceMat.channels() * sourceMat.total());
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
        for (int a = 0; a < (10); a++) {
            for (int b = 0; b < (10); b++) {
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


        // Nilai Variable untuk dehaze di seekbar gimana ??
        // nilai tidak cuman satu
        // nilai masuk kemana di programnya mana

        // nilai hanya threshold nya saja untuk nge hapus dehaze nya


        // return bitmap hasil dehaze pake lib nya..
        results[2] = new ImageDehazeResult(hazeRemover.dehaze(pixels, src.getHeight(), src.getWidth()));


//        return new ImageDehazeResult(hazeRemover.dehaze(pixels, src.getHeight(), src.getWidth(), threshold));
        return results;
    }



    // FIXME Diwang nambahin asynctask buat ngeload image hasil potret ke imageView nya
    private class OriginalImageLoaderThread extends AsyncTask<Void, Void, Bitmap> {

        public OriginalImageLoaderThread() {
            imageUrl = UriToUrl.get(getApplicationContext(), imageUri);
            bitmapLoader = new BitmapLoader();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                // proses utama, nge bikin gambar dari URL yang udah kita dapet dari hasil potret
                return bitmapLoader.load(getApplicationContext(), new int[]{imageView.getWidth(), imageView.getHeight()}, imageUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap hasilLoadDariUrl) {
            super.onPostExecute(hasilLoadDariUrl);

            // udah dapet nih gambar bagusnya di @bitmap di atas

            Log.d("Load ori image","Dapet bitmapnya!" + hasilLoadDariUrl.getWidth() + ", Height: " + hasilLoadDariUrl.getHeight());
//            Log.d("Width n Height")

            imageView.setImageBitmap(hasilLoadDariUrl);
            originalBitmap = hasilLoadDariUrl;
        }
    }
    // syntax nampilin hasil dehaze
    private class DehazedImageLoaderThread extends AsyncTask<Bitmap, Void, ImageDehazeResult> {
        float params_value;
        public DehazedImageLoaderThread(float threshold) {
            imageUrl = UriToUrl.get(getApplicationContext(), imageUri);
            bitmapLoader = new BitmapLoader();
            params_value = threshold;
        }

        @Override
        protected ImageDehazeResult doInBackground(Bitmap... bitmapAsliDariThreadSblmnya) {
            // Menerima bitmap original yang didapat dari proses OriginalBitmapLoader (thread sebelumnya)
            Bitmap bitmap = bitmapAsliDariThreadSblmnya[0];
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            // kirim ke PostExecute di bawah
            return new ImageDehazeResult(hazeRemover.dehaze(pixels, bitmap.getHeight(), bitmap.getWidth(), params_value));
        }

        @Override
        protected void onPostExecute(ImageDehazeResult imageDehazeResult) {
            super.onPostExecute(imageDehazeResult);

            // set image hasil dehaze image sebelahnya
            imageViewDehaze.setImageBitmap(imageDehazeResult.getResult());

            // set image depth hasil dehaze ke sebelahnya lagi
            imageViewDepthMap.setImageBitmap(imageDehazeResult.getDepth());

            // udahan progress bar nya, muter mulu.
            progressBar.setVisibility(View.GONE);
            // Cari filter lain, cari parameter nya apa bandingkan dengan cara kualitatif ( masih nyoba on progress )
            // Nilai kabutnya darimana (threshold) , cek dengan rumus nya kabut nya masih ada apa engga
            // Cara validasi kabut nya dari metode nya gimana ( depth map )

            // Validasi kabut ambil dari threshold transmission nya sama liat dari depth map

            // tulis di buku TA algoritma nyaa

            // process tiap gambar - dehaze gimana
//            ImageDehazeResult[] resultDehazed = removeHazeOnBitmap(originalBitmap, 100);
//            imageViewProcess1.setImageBitmap(resultDehazed[0].getResult());
//            imageViewProcess2.setImageBitmap(resultDehazed[1].getResult());
//            imageView.setImageBitmap(resultDehazed[2].getResult());
            getMatrik(originalBitmap);
        }
    }


}
