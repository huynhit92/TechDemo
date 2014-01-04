package com.example.camerapreviewdemo;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.ZoomControls;

public class MainActivity extends Activity {
	
	private Preview mPreview;
	private Camera mCamera;
    private int numberOfCameras;
    private int cameraCurrentlyLocked;
    private ZoomControls mZoomControl;
    private int currentZoomLevel;	
    
    // The first rear facing camera
    int defaultCameraId;
    private Button mBtnTakePicture;
    private Button mBtnSwitchCamera;
    
    private final String TAG = "CameraStatus";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FrameLayout previewLayout = (FrameLayout)findViewById(R.id.preview);
		mPreview = new Preview(MainActivity.this);
		previewLayout.addView(mPreview);
		
		// Find the total number of cameras available
        numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }
        
        initButtons();
	}
	
	private void initButtons(){
		mBtnTakePicture = (Button)findViewById(R.id.button_take_picture);
		mBtnSwitchCamera = (Button)findViewById(R.id.button_switch_camera);
		
		mBtnTakePicture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPreview.takePicture();
			}
		});
		
		mBtnSwitchCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 // check for availability of multiple cameras
	            if (numberOfCameras == 1) {
	                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	                builder.setMessage(getString(R.string.camera_alert))
	                       .setNeutralButton("Close", null);
	                AlertDialog alert = builder.create();
	                alert.show();
	            }

	            // OK, we have multiple cameras.
	            // Release this camera -> cameraCurrentlyLocked
	            if (mCamera != null) {
	                mCamera.stopPreview();
	                mPreview.setCamera(null);
	                mCamera.release();
	                mCamera = null;
	            }

	            // Acquire the next camera and request Preview to reconfigure
	            // parameters
	            
	            mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
	            cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)% numberOfCameras;
	            mPreview.switchCamera(mCamera);

	            // Start the preview
	            mCamera.startPreview();
			}
		});
		
		mZoomControl = (ZoomControls)findViewById(R.id.zoom_control);
		if(mCamera != null){
			Log.i(TAG, "It is opened");
		}
		else{
			Log.i(TAG,"Not opend");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
	
		return true;
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Open the default i.e. the first rear facing camera.
        mCamera = Camera.open();
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
        
        initZoomControl();
	}
	
	private void initZoomControl(){
		if(mCamera == null){
			return;
		}
		Camera.Parameters camParam = mCamera.getParameters();
        if(camParam != null){
        	if(camParam.isZoomSupported()){
        		mZoomControl.setVisibility(View.VISIBLE);
        		final int maxZoomLevel = camParam.getMaxZoom();
        		mZoomControl.setIsZoomInEnabled(true);
        		mZoomControl.setIsZoomOutEnabled(true);
        		
        		mZoomControl.setOnZoomInClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(currentZoomLevel < maxZoomLevel){
							currentZoomLevel ++;
							mPreview.setCameraZoom(currentZoomLevel);
						}
					}
				});
        		
        		mZoomControl.setOnZoomOutClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(currentZoomLevel > 0){
							currentZoomLevel --;
							mPreview.setCameraZoom(currentZoomLevel);
						}
					}
				});
        		
        	}else{
        		mZoomControl.setVisibility(View.GONE);
        	}
        }
	}

}
