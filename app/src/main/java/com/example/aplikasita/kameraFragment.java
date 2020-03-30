package com.example.aplikasita;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class kameraFragment extends Fragment {

    private Uri imageUri;
    private ImageView imageView1, show_graph;
    private Button btnTakepic;
    private Button histBtn;
    private Button Gofilter;
    private boolean isImageHistogram = false;
    private Bitmap imageBitmap;
//    private ImageView inputImg, inputImg1, outputImg, outputImg1, iv1, iv2, iv3, iv4;
//    private Bitmap inputBmp, bmp1, bmp2;
//    private Size mSize0;
//    private Mat mIntermediateMat;
//    private Mat mMat0;
//    private MatOfInt[] mChannels;
//    private MatOfInt mHistSize;
//    private int mHistSizeNum = 25;
//    private MatOfFloat mRanges;
//    private Scalar[] mColorsRGB;
//    private Scalar[] mColorsHue;
//    private Scalar mWhite;
//    private Point mP1;
//    private Point mP2;
//    private float[] mBuff;

    private static final int TAKE_PICTURE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final int requestCode = 20;
    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_PERMISSION = 200;
    private String imageFilePath = "";


    // OpenCV
//    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
//        @Override
//        public void onManagerConnected(int status) {
//            super.onManagerConnected(status);
//        }
//    };


    public kameraFragment() {
        // Required empty public constructor
    }
//    Mat mat = new Mat();
//    Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
//    Utils.bitmapToMat(bmp32, mat);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kamera, container, false);
        imageView1 = view.findViewById(R.id.imageView1);
        show_graph = view.findViewById(R.id.show_graph);
        btnTakepic = view.findViewById(R.id.btnTakepic);
        Gofilter = view.findViewById(R.id.Gofilter);


        //Buat Take Camera
        btnTakepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
//            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(cameraIntent, TAKE_PICTURE);
            }
        });

        // Button untuk ke Activity GoFilter
        Gofilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), option.class);
//                intent.putExtra("imageUri", imageUri.toString());
//                startActivity(intent);
                if(imageView1.getDrawable() == null)
                {
//                    intent.putExtra("imageUri", imageUri.toString());
                    startActivity(intent);
                }else {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView1.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    Log.d("bitmap", bitmap.getWidth() + " " + bitmap.getHeight());
                    intent.putExtra("image", bitmap);
                    startActivity(intent);
                }
//                else {
//                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView1.getDrawable();
//                    Bitmap imageBitmap = bitmapDrawable.getBitmap();
//                    intent.putExtra("image2", imageBitmap);
//                    startActivity(intent);
//                }
            }
        });

        // Buat Open Gallery
//        public void openGaleri(View view) {
//            Intent myIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(myIntent, 100);
//
//
//        }


        return view;
    }

    //Passing Intent ke GoFilter Activity
    private ByteArrayInputStream convertDrawable(ImageView image2){
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) image2.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        return new ByteArrayInputStream(imageInByte);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    // Menampilkan Pilihan di button Take Photo/Open Gallery
    private void selectImage () {
        final CharSequence[] options = { "Take Photo", "Open Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                }
                else if (options[which].equals("Open Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[which].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    // Nampilin Histogram sama Imageview1
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 1){
                if (data.getExtras().get("data")!=null){
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    imageView1.setImageBitmap(bitmap);
                    getMatrik(bitmap);
                    histogram(bitmap);
                    Gofilter.setEnabled(true);
                    Log.d("Rotate", bitmap.getWidth() + ";" + bitmap.getHeight());
                    Log.d("reza", bitmap.toString());
                    Log.d("reza",  "tes");
                }
            }else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data !=null  ){
                    imageUri = data.getData();
                    try {
                       imageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                       imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int)(imageBitmap.getWidth()*0.4), (int)(imageBitmap.getHeight()*0.4), true);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    Log.d("Rotate", imageBitmap.getWidth() + ";" + imageBitmap.getHeight());
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(-90);
//                    imageBitmap =  Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

                imageView1.setImageBitmap(imageBitmap);
                    histogram(imageBitmap);
                    getMatrik(imageBitmap);
                    Gofilter.setEnabled(true);
                }

            }
        }
       // Menampilkan Matrix ketika Take Photo dan Open Gallery
        private void getMatrik(Bitmap imageBitmap){
            Mat mat = new Mat();
            Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, mat);

            // FIXME Diwang komenin get matrix ini supaya gampang load
//            Log.d("Matrik", Arrays.toString(mat.get(mat.rows(), mat.cols())));
//            for (int a=0 ; a<mat.rows();a++){
//                for (int b=0 ; b<mat.cols();b++){
//                    Log.d("Matrik", "["+a+"]"+"["+b+"]"+Arrays.toString(mat.get(a, b)));
//                }
//            }
        }


     // Perhitungan Histogram

    public void histogram(Bitmap imageBitmap) {
        if (imageBitmap != null) {
            if (!isImageHistogram) {
                //add histogram code
                Mat sourceMat = new Mat();
                Utils.bitmapToMat(imageBitmap, sourceMat);

                Size sourceSize = sourceMat.size();

                int histogramSize = 256;
                MatOfInt hisSize = new MatOfInt(histogramSize);

                Mat destinationMat = new Mat();
                List<Mat> channels = new ArrayList<>();

                MatOfFloat range = new MatOfFloat(0f, 255f);
                MatOfFloat histRange = new MatOfFloat(range);

                Core.split(sourceMat, channels);

                MatOfInt[] allChannel = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
                Scalar[] colorScalar = new Scalar[]{new Scalar(220, 0, 0, 255), new Scalar(0, 220, 0, 255), new Scalar(0, 0, 220, 255)};

                Mat matB = new Mat(sourceSize, sourceMat.type());
                Mat matG = new Mat(sourceSize, sourceMat.type());
                Mat matR = new Mat(sourceSize, sourceMat.type());

                Imgproc.calcHist(channels, allChannel[0], new Mat(), matB, hisSize, histRange);
                Imgproc.calcHist(channels, allChannel[1], new Mat(), matG, hisSize, histRange);
                Imgproc.calcHist(channels, allChannel[2], new Mat(), matR, hisSize, histRange);


                int graphHeight = 300;
                int graphWidth = 400;
                int binWidth = 3;

                Mat graphMat = new Mat(graphHeight, graphWidth, CvType.CV_8UC3, new Scalar(0, 0, 0));

                //Normalize channel
                Core.normalize(matB, matB, graphMat.height(), 0, Core.NORM_INF);
                Core.normalize(matG, matG, graphMat.height(), 0, Core.NORM_INF);
                Core.normalize(matR, matR, graphMat.height(), 0, Core.NORM_INF);

                //convert pixel value to point and draw line with points
                for (int i = 0; i < histogramSize; i++) {
                    Point bPoint1 = new Point(binWidth * (i - 1), graphHeight - Math.round(matB.get(i - 1, 0)[0]));
                    Point bPoint2 = new Point(binWidth * i, graphHeight - Math.round(matB.get(i, 0)[0]));
                    Imgproc.line(graphMat, bPoint1, bPoint2, new Scalar(220, 0, 0, 255), 3, 8, 0);

                    Point gPoint1 = new Point(binWidth * (i - 1), graphHeight - Math.round(matG.get(i - 1, 0)[0]));
                    Point gPoint2 = new Point(binWidth * i, graphHeight - Math.round(matG.get(i, 0)[0]));
                    Imgproc.line(graphMat, gPoint1, gPoint2, new Scalar(0, 220, 0, 255), 3, 8, 0);

                    Point rPoint1 = new Point(binWidth * (i - 1), graphHeight - Math.round(matR.get(i - 1, 0)[0]));
                    Point rPoint2 = new Point(binWidth * i, graphHeight - Math.round(matR.get(i, 0)[0]));
                    Imgproc.line(graphMat, rPoint1, rPoint2, new Scalar(0, 0, 220, 255), 3, 8, 0);
                }

                //convert Mat to bitmap
                Bitmap graphBitmap = Bitmap.createBitmap(graphMat.cols(), graphMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(graphMat, graphBitmap);

                // show histogram
                show_graph.setImageBitmap(graphBitmap);
                //set the isImageHistogram
                isImageHistogram = false;

            }
        }


//    private void HistogramVariableInitialization() {
//        mIntermediateMat = new Mat();
//        mSize0 = new Size();
//        mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
//        mBuff = new float[mHistSizeNum];
//        mHistSize = new MatOfInt(mHistSizeNum);
//        mRanges = new MatOfFloat(0f, 256f);
//        mMat0 = new Mat();
//        mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
//        mColorsHue = new Scalar[] {
//                new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
//                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
//                new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
//                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
//                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
//        };
//        mWhite = Scalar.all(255);
//        mP1 = new Point();
//        mP2 = new Point();
//    }
//
//    private void Histogram(Bitmap bmp, ImageView iv) {
//        Bitmap bmp3 = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
//        int imgH = bmp3.getHeight();
//        int imgW = bmp3.getWidth();
//        Mat rgba = new Mat(imgH, imgW, CvType.CV_8UC1);
//        Utils.bitmapToMat(bmp, rgba);
//
//        Size sizeRgba = rgba.size();
//        Mat rgbaInnerWindow;
//        int rows = (int) sizeRgba.height;
//        int cols = (int) sizeRgba.width;
//        int left = cols / 8;
//        int top = rows / 8;
//        int width = cols * 3 / 4;
//        int height = rows * 3 / 4;
//
//        Mat hist = new Mat();
//        int thickness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
//        if (thickness > 5) thickness = 5;
//        int offset = (int) ((sizeRgba.width - (5 * mHistSizeNum + 4 * 10) * thickness) / 2);
//
//        for (int c = 0; c < 3; c++) {
//            Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
//            Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
//            hist.get(0, 0, mBuff);
//            for (int h = 0; h < mHistSizeNum; h++) {
//                mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thickness;
//                mP1.y = mP2.y = sizeRgba.height - 1;
//                mP2.y = mP1.y - 2 - (int) mBuff[h];
//                Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thickness);
//            }
//        }
//
//        Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
//        Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
//        Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
//        hist.get(0, 0, mBuff);
//
//        for (int h = 0; h < mHistSizeNum; h++) {
//            mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thickness;
//            mP1.y = sizeRgba.height - 1;
//            mP2.y = mP1.y - 2 - (int) mBuff[h];
//            Imgproc.line(rgba, mP1, mP2, mWhite, thickness);
//        }
//
//        // Hue
//        Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
//        Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
//        hist.get(0, 0, mBuff);
//
//        for (int h = 0; h < mHistSizeNum; h++) {
//            mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) * h) * thickness;
//            mP1.y = sizeRgba.height - 1;
//            mP2.y = mP1.y - 2 - (int) mBuff[h];
//            Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thickness);
//        }
//
//        try {
//            bmp3 = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(rgba, bmp3);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        iv.setImageBitmap(bmp3);
//        iv.invalidate();
//    }
// end Histogram
    }
}