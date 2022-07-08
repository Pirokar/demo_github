package im.threads.internal.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.threads.R;
import im.threads.internal.helpers.FileHelper;
import im.threads.internal.image_loading.CoilImageLoader;
import im.threads.internal.image_loading.ImageLoader;
import im.threads.internal.utils.ThreadsLogger;

public final class CameraActivity extends BaseActivity {
    public static final String IMAGE_EXTRA = "IMAGE_EXTRA";
    public static final int FLASH_ON = 1;
    public static final int FLASH_OFF = 2;
    public static final int FLASH_AUTO = 3;
    private static final String TAG = "CameraActivity ";
    private static final String SELFIE_MODE_EXTRA = "SELFIE_MODE_EXTRA";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private int mFlashMode = 3;
    private boolean isFrontCamera;
    private boolean isCameraReleased = false;
    private String mCurrentPhoto;
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private boolean selfieMode = false;
    private ImageLoader imageLoader = new CoilImageLoader();

    public static Intent getStartIntent(Context context, boolean selfieMode) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(SELFIE_MODE_EXTRA, selfieMode);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfieMode = getIntent().getBooleanExtra(SELFIE_MODE_EXTRA, false);
        isFrontCamera = selfieMode;
        setContentView(R.layout.activity_camera);
        initPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStateCameraPreview();
        if (isCameraReleased) {
            restoreCamera();
        }
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

    private void initPreview() {
        FrameLayout flCamera = findViewById(R.id.fl_camera);
        ViewGroup.LayoutParams lp = flCamera.getLayoutParams();
        lp.width = getTargetWidth();
        lp.height = getTargetHeight();
        mSurfaceView = findViewById(R.id.camera_preview);
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
                    ThreadsLogger.e(TAG, "error while setting preview display of camera", e);
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
        if (selfieMode) {
            findViewById(R.id.label_top_selfie).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_selfie_mask).setVisibility(View.VISIBLE);
            findViewById(R.id.label_top).setVisibility(View.GONE);
        } else {
            findViewById(R.id.label_top).setVisibility(View.VISIBLE);
            findViewById(R.id.label_top_selfie).setVisibility(View.GONE);
            findViewById(R.id.ll_selfie_mask).setVisibility(View.GONE);
        }
        findViewById(R.id.bottom_buttons_photo).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_buttons_image).setVisibility(View.GONE);
        final ImageButton flashButton = findViewById(R.id.flash_control);
        final ImageButton takePhotoButton = findViewById(R.id.take_photo);
        ImageButton switchCamButton = findViewById(R.id.switch_cams);
        if (Camera.getNumberOfCameras() == 0) {
            Toast.makeText(this, getResources().getString(R.string.threads_no_cameras_detected), Toast.LENGTH_SHORT).show();
            finish();
        }
        if (selfieMode) {
            switchCamButton.setVisibility(View.INVISIBLE);
            flashButton.setVisibility(View.INVISIBLE);
        }
        switchCamButton.setOnClickListener(v -> {
            int currentCameraId;
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
                    ThreadsLogger.e(TAG, "error while switching cameras", e);
                    finish();
                }
            }
        });
        takePhotoButton.setEnabled(true);
        takePhotoButton.setOnClickListener(v -> {
            takePhotoButton.setEnabled(false);
            mCamera.takePicture(() -> {
                    }
                    , (data, camera) -> {
                    }
                    , (data, camera) -> mExecutor.execute(() -> {
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
                        File output = FileHelper.INSTANCE.createImageFile(CameraActivity.this);
                        try {
                            FileOutputStream fio = new FileOutputStream(output);
                            out.compress(Bitmap.CompressFormat.JPEG, 100, fio);
                            try {
                                fio.flush();
                            } catch (IOException e) {
                                ThreadsLogger.e(TAG, "onPictureTaken", e);
                            }
                            mCurrentPhoto = output.getAbsolutePath();
                        } catch (FileNotFoundException e) {
                            ThreadsLogger.e(TAG, "error while saving image to disk", e);
                        }
                        final File finalOutput = output;
                        new Handler(Looper.getMainLooper()).post(() -> setStateImagePreview(finalOutput.getAbsolutePath()));
                    }));
        });
        flashButton.setOnClickListener(v -> mCamera.setParameters(setFlashState(mFlashMode, mCamera.getParameters())));
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
        ImageButton flashButton = findViewById(R.id.flash_control);
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
        findViewById(R.id.ll_selfie_mask).setVisibility(View.GONE);
        findViewById(R.id.bottom_buttons_photo).setVisibility(View.GONE);
        ImageView image = findViewById(R.id.photo_preview);
        image.setVisibility(View.VISIBLE);
        imageLoader.loadFile(
                image,
                new File(imagePath),
                List.of(ImageView.ScaleType.FIT_XY),
                null,
                null
        );
        findViewById(R.id.bottom_buttons_image).setVisibility(View.VISIBLE);
        Button retakeButton = findViewById(R.id.retake);
        retakeButton.setOnClickListener(v -> {
            setStateCameraPreview();
            mCamera.startPreview();
            mCurrentPhoto = null;
        });
        Button sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            Intent i = new Intent();
            i.putExtra(IMAGE_EXTRA, mCurrentPhoto);
            setResult(RESULT_OK, i);
            finish();
        });
    }

    private void restoreCamera() {
        releaseCamera();
        try {
            mCamera = Camera.open(isFrontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
            setUpCameraInitialParameters();
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mCamera.startPreview();
            isCameraReleased = false;
            mCamera.setParameters(setFlashState(mFlashMode, mCamera.getParameters()));
        } catch (IOException e) {
            ThreadsLogger.e(TAG, "restoreCamera", e);
        } catch (RuntimeException ex) {
            String error = getResources().getString(isFrontCamera ?
                    R.string.threads_front_camera_could_not_start_error
                    : R.string.threads_back_camera_could_not_start_error);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            ThreadsLogger.e(TAG, "restoreCamera", ex);
        }
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
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, getTargetWidth(), getTargetHeight());
        cp.setPreviewSize(optimalSize.width, optimalSize.height);
        cp.setPictureSize(optimalSize.width, optimalSize.height);
        mCamera.setParameters(cp);
        mCamera.setDisplayOrientation(90);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.width - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.width - h);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.width - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - h);
                }
            }
        }
        return optimalSize;
    }

    private int getTargetWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int getTargetHeight() {
        return getResources().getDisplayMetrics().widthPixels * 4 / 3;
    }
}
