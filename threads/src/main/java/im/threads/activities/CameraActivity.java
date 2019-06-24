package im.threads.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.threads.R;
import im.threads.helpers.FileHelper;
import im.threads.picasso_url_connection_only.Picasso;

/**
 * Created by yuri on 08.07.2016.
 */
public class CameraActivity extends BaseActivity {
    private static final String TAG = "CameraActivity ";
    public static final String IMAGE_EXTRA = "IMAGE_EXTRA";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private int mFlashMode = 3;
    public static final int FLASH_ON = 1;
    public static final int FLASH_OFF = 2;
    public static final int FLASH_AUTO = 3;
    private boolean isFrontCamera;
    private boolean isCameraReleased = false;
    private String mCurrentPhoto;
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        t.setTitle("");
        initPreview();
    }

    private void initPreview() {
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        mSurfaceView.setVisibility(View.VISIBLE);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (mSurfaceView.getVisibility() == View.VISIBLE) {
                        releaseCamera();
                        mCamera = Camera.open(isFrontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
                        isCameraReleased = false;
                        mCamera.setPreviewDisplay(holder);
                        setUpCameraInitialParameters();
                        mCamera.startPreview();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "error while setting preview display of camera");
                    e.printStackTrace();
                    finish();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    private void setStateCameraPreview() {
        findViewById(R.id.photo_preview).setVisibility(View.GONE);
        findViewById(R.id.label_top).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_buttons_photo).setVisibility(View.VISIBLE);
        findViewById(R.id.toolbar).setVisibility(View.GONE);
        findViewById(R.id.bottom_buttons_image).setVisibility(View.GONE);
        final ImageButton flashButton = (ImageButton) findViewById(R.id.flash_control);
        final ImageButton takePhotoButton = (ImageButton) findViewById(R.id.take_photo);
        ImageButton switchCamButton = (ImageButton) findViewById(R.id.switch_cams);
        if (Camera.getNumberOfCameras() == 0) {
            Toast.makeText(this, getResources().getString(R.string.threads_no_cameras_detected), Toast.LENGTH_SHORT).show();
            finish();
        }
        switchCamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.CameraInfo currentCamInfo = new Camera.CameraInfo();
                int currentCameraId = -1;
                if (Camera.getNumberOfCameras() > 1) {
                    releaseCamera();
                    if (isFrontCamera) {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    } else {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    }
                    isFrontCamera = !isFrontCamera;
                    mCamera = Camera.open(currentCameraId);
                    isCameraReleased = false;
                    setUpCameraInitialParameters();
                    try {
                        mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                        mCamera.startPreview();
                    } catch (IOException e) {
                        Log.e(TAG, "error while switching cameras");
                        e.printStackTrace();
                        finish();
                    }
                }
            }
        });
        takePhotoButton.setEnabled(true);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoButton.setEnabled(false);
                mCamera.takePicture(new Camera.ShutterCallback() {
                                        @Override
                                        public void onShutter() {

                                        }
                                    }
                        , new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {

                            }
                        }
                        , new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(final byte[] data, Camera camera) {
                                mExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap raw = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        Bitmap out = raw;
                                        if (raw.getWidth() > raw.getHeight() && !isFrontCamera) {
                                            Matrix m = new Matrix();
                                            m.setRotate(90);
                                            out = Bitmap.createBitmap(raw, 0, 0, raw.getWidth(), raw.getHeight(), m, false);
                                            raw.recycle();
                                        } else if (isFrontCamera && raw.getWidth() > raw.getHeight()) {
                                            float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                                            Matrix matrix = new Matrix();
                                            Matrix matrixMirrorY = new Matrix();
                                            matrixMirrorY.setValues(mirrorY);
                                            matrix.postConcat(matrixMirrorY);
                                            matrix.postRotate(90);
                                            out = Bitmap.createBitmap(raw, 0, 0, raw.getWidth(), raw.getHeight(), matrix, true);
                                            raw.recycle();
                                        }
                                        File output = FileHelper.createImageFile(CameraActivity.this);
                                        try {
                                            FileOutputStream fio = new FileOutputStream(output);
                                            out.compress(Bitmap.CompressFormat.JPEG, 100, fio);
                                            try {
                                                fio.flush();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            mCurrentPhoto = output.getAbsolutePath();
                                        } catch (FileNotFoundException e) {
                                            Log.e(TAG, "error while saving image to disk");
                                            e.printStackTrace();
                                        }
                                        final File finalOutput = output;
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                setStateImagePreview(finalOutput.getAbsolutePath());
                                            }
                                        });
                                    }
                                });
                            }
                        });
            }
        });
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.setParameters(setFlashState(mFlashMode, mCamera.getParameters()));
            }
        });

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        isCameraReleased = true;
    }

    private Camera.Parameters setFlashState(int state, Camera.Parameters p) {
        ImageButton flashButton = (ImageButton) findViewById(R.id.flash_control);
        List<String> supportedFlashParams = mCamera.getParameters().getSupportedFlashModes();
        if (supportedFlashParams == null) {
            supportedFlashParams = new ArrayList<>();
        }
        switch (state) {
            case FLASH_ON:
                mFlashMode = FLASH_OFF;
                flashButton.setImageResource(R.drawable.ic_flash_off_white_30dp);
                p = mCamera.getParameters();
                if (supportedFlashParams.contains(Camera.Parameters.FLASH_MODE_OFF))
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                return p;
            case FLASH_OFF:
                mFlashMode = FLASH_AUTO;
                flashButton.setImageResource(R.drawable.ic_flash_auto_white_30dp);
                p = mCamera.getParameters();
                if (supportedFlashParams.contains(Camera.Parameters.FLASH_MODE_AUTO))
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                return p;
            case FLASH_AUTO:
                mFlashMode = FLASH_ON;
                flashButton.setImageResource(R.drawable.ic_flash_on_white_30dp);
                p = mCamera.getParameters();
                if (supportedFlashParams.contains(Camera.Parameters.FLASH_MODE_ON))
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                return p;
        }
        return p;
    }

    private void setStateImagePreview(String imagePath) {
        findViewById(R.id.label_top).setVisibility(View.GONE);
        //   mSurfaceView.setVisibility(View.GONE);
        findViewById(R.id.bottom_buttons_photo).setVisibility(View.GONE);
        ImageView image = (ImageView) findViewById(R.id.photo_preview);
        image.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load(new File(imagePath))
                .fit()
                .centerCrop()
                .into(image);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setVisibility(View.GONE);
        t.setTitle("");
        findViewById(R.id.bottom_buttons_image).setVisibility(View.VISIBLE);
        Button retakeButton = (Button) findViewById(R.id.retake);
        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStateCameraPreview();
                mCamera.startPreview();
                mCurrentPhoto = null;
            }
        });
        Button sendButton = (Button) findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra(IMAGE_EXTRA, mCurrentPhoto);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSurfaceView.getVisibility() == View.VISIBLE) {
            if (mCamera == null) {
                Toast.makeText(this, getString(R.string.threads_no_cameras_detected), Toast.LENGTH_SHORT).show();

            } else {
                releaseCamera();
                isCameraReleased = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStateCameraPreview();
        if (isCameraReleased) {
            restoreCamera();
        }
    }

    private void restoreCamera() {
        releaseCamera();
        mCamera = Camera.open(isFrontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        try {
            setUpCameraInitialParameters();
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mCamera.startPreview();
            isCameraReleased = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.setParameters(setFlashState(mFlashMode, mCamera.getParameters()));
    }

    private void setUpCameraInitialParameters() {
        mCamera.enableShutterSound(true);
        Camera.Parameters cp = mCamera.getParameters();
        List<String> supportedFlashMode = cp.getSupportedFlashModes();
        if (null != supportedFlashMode) {
            if (supportedFlashMode.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                cp.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            } else {
                cp.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }
        List<String> supportedFocusModes = cp.getSupportedFocusModes();
        if (null != supportedFocusModes) {
            for (String mode : supportedFocusModes) {
                if (mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    cp.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
            }
        }
        List<Camera.Size> sizes = cp.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes,
                getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);
        cp.setPreviewSize(optimalSize.width, optimalSize.height);
        cp.setPictureSize(optimalSize.width, optimalSize.height);
        mCamera.setParameters(cp);
        mCamera.setDisplayOrientation(90);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.width - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.width - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.width - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
