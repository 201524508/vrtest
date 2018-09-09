package com.example.yujaya.dddd;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

public class VrPanoramaActivity extends Activity {
    private static final String TAG = "VRTESTACTIVITY";

    private VrPanoramaView panoramaView;
    private ImageActivity imgActivity;
    private Bitmap[] imgArray;
    private Bitmap img;
    public boolean loadImageSuccessful;

    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_layout);
        this.imgArray = MainActivity.imgArray;
        panoramaImg();

        if(img == null){
            img = BitmapFactory.decodeResource(this.getResources(), R.drawable.waiting);
        }

        panoramaView = (VrPanoramaView)findViewById(R.id.pano_view);
        panoramaView.setEventListener(new ActivityEventListener());

        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
        panoramaView.loadImageFromBitmap(img, panoOptions);
        panoramaView.setDisplayMode(3);
    }

    public void panoramaImg(){  //파노라마 이미지 생성
        imgActivity = new ImageActivity(imgArray);

        img = imgActivity.makePanorama();

        for(int i = 3; i >= 0; i--){
            imgArray[i].recycle();
            imgArray[i] = null;
        }
    }

    @Override
    protected void onPause() {
        panoramaView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoramaView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoramaView.shutdown();
        super.onDestroy();
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    VrPanoramaActivity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }
}
