package com.example.myvideorecorder;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_VIDEO_CAPTURE = 1;
    VideoView mVideoView;
    ImageView iv_disp;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mVideoView = findViewById(R.id.disp_vv);
//        capturedImageView = findViewById(R.id.img_disp_iv);

//        dispatchTakeVideoIntent();
        iv_disp = findViewById(R.id.disp_iv);

        ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,1);

    }

    @Override
    protected void onStart() {
        super.onStart();

        deFrameVideo("android.resource://"+getPackageName()+"/raw/fight");

    }

    private void deFrameVideo(String path)
    {
        Uri uri = Uri.parse(path);
        //Log.d("path = ",uri+"");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String video_duration="";
        try {
            retriever.setDataSource(this,uri);
            video_duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            Log.d("Duration of video = ",video_duration);
        }
        catch (IllegalArgumentException iae)
        {
            Log.d("Status","NOPE");
            iae.printStackTrace();
        }


        Bitmap bmpOriginal = retriever.getFrameAtTime(0);
        int bmpVideoHeight = bmpOriginal.getHeight();
        int bmpVideoWidth = bmpOriginal.getWidth();
        byte [] lastSavedByteArray = new byte[0];

        float factor = 20f;
        int scaleWidth = (int) ( (float) bmpVideoWidth * factor );
        int scaleHeight = (int) ( (float) bmpVideoHeight * factor );
        int max = (int) Long.parseLong(video_duration);

        for(int i=0;i<max;i+=1000) {

            bmpOriginal = retriever.getFrameAtTime(i*1000,MediaMetadataRetriever.OPTION_CLOSEST);
            bmpVideoHeight = (bmpOriginal==null)?-1:bmpOriginal.getHeight();
            bmpVideoWidth = (bmpOriginal==null)?-1:bmpOriginal.getWidth();
            int byteCount = bmpVideoHeight*bmpVideoWidth*4;
            ByteBuffer tempByteBuffer = ByteBuffer.allocate(byteCount);
            if (bmpOriginal == null)
                continue;
            bmpOriginal.copyPixelsToBuffer(tempByteBuffer);
            byte[] tempByteArray = tempByteBuffer.array();

            if(!Arrays.equals(tempByteArray,lastSavedByteArray)) {

                File outputfile = new File("/sdcard/android/", "fight_"+i+".jpeg");
                OutputStream out = null;
                try {
                    out = new FileOutputStream(outputfile);
                    Log.d("FILE", "Found");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bmpScaledSize = Bitmap.createScaledBitmap(bmpOriginal, scaleWidth, scaleHeight, false);
                bmpScaledSize.compress(Bitmap.CompressFormat.PNG, 100, out);

                try {
                    assert out != null;
                    Log.d("IMAGE STATUS", "Successful");
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lastSavedByteArray = tempByteArray;

            }
        }

        //iv_disp.setImageBitmap(bmpOriginal);
        retriever.release();
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public void combineFramesPlay(View view) {
        String filePath="fight_0.jpeg";
        File inputFile = new File("/sdcard/android/",filePath);
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/android/fight_0.jpeg");
        iv_disp.setImageBitmap(bitmap);
//        SequenceEncoder
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            mVideoView.setVideoURI(videoUri);
            mVideoView.start();
            mVideoView.setOnClickListener(new View.OnClickListener() {
                boolean paused = false;
                @Override
                public void onClick(View v) {
                    if(v == mVideoView)
                        Log.d("V==mVideoView","true");
                    else
                        Log.d("V==mVideoView","false");
                    if (!paused) {
                        mVideoView.pause();
                        paused = true;
                    }
                    else {
                        if (mVideoView.isPlaying())
                            mVideoView.resume();
                        else
                            mVideoView.start();
                        paused = false;
                    }

                }
            });
            Log.d("VIDEO PLAYING STATUS",""+mVideoView.isPlaying());
        }
    }*/
}
