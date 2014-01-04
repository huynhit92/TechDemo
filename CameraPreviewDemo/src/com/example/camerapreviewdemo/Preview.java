package com.example.camerapreviewdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback{
	private Camera mCamera;
	private SurfaceHolder mHolder;
	Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
	private final String TAG = "PreviewCamera";
	
	public Preview(Context context){
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
	}



	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        mCamera.setParameters(parameters);
        mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
            mCamera.stopPreview();
        }
	}

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    
    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        camera.setParameters(parameters);
     }
    
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
        	mCamera.setDisplayOrientation(90);
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }
    
    public void setCameraZoom(int zoomLevel){
    	if(mCamera == null){
    		return;
    	}
    	
    	try{
    		Parameters param = mCamera.getParameters();
    		param.setZoom(zoomLevel);
    		mCamera.setParameters(param);
    	}catch(Exception e){
    		Log.e(TAG, "" + e.getMessage());
    	}
    }
    
    PictureCallback pictureCallback = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			String sFileName = createFileName();
			if(sFileName.equals("")){
				return;
			}
			
			FileOutputStream outStream = null;
			try{
				outStream = new FileOutputStream(sFileName);
				outStream.write(data);
				outStream.close();
				camera.startPreview();
			}catch(Exception e){
				Log.e(TAG, "" + e.getMessage());
			}
		}
	};
	
	PictureCallback rawCallback = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.v(TAG, "onPictureTaken");
		}
	};
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		
		@Override
		public void onShutter() {
			Log.i(TAG, "onShutter");
		}
	};
    public void takePicture(){
    	if(mCamera == null){
    		return;
    	}
    	
    	mCamera.takePicture(shutterCallback, rawCallback, pictureCallback);
    }
    
    private String createFileName(){
    	
    	String state = Environment.getExternalStorageState();
    	if(state.equals(Environment.MEDIA_MOUNTED)){
    		String sFileName = Environment.getExternalStorageDirectory() + File.separator + System.currentTimeMillis() + ".jpg";
    		return sFileName;
    	}
    	return "";
    }
}
