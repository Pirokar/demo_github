package im.threads.internal.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.threads.internal.utils.ThreadsLogger;

public final class MediaHelper {

    private static final String TAG = MediaHelper.class.getSimpleName();

    public static int PHOTO_RESIZE_MAX_SIDE = 1600;

    private static final String IMAGE_RESIZE_CACHE_DIR_NAME = "imageResizeCache";

    public static void grantPermissions(final Context context, final Intent intent, final Uri imageUri) {
        final List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (!resInfoList.isEmpty()) {
            for (final ResolveInfo resolveInfo : resInfoList) {
                final String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    public static File downsizeImage(Context context, File imageFile, int maxSidePx) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
        int imageMaxSize = maxSidePx * maxSidePx;
        // Determine how much to prescale down the image to avoid OutOfMemory error
        int preloadScaleFactor = 1;
        while ((bmOptions.outWidth * bmOptions.outHeight) * (1 / Math.pow(preloadScaleFactor, 2)) >
                imageMaxSize) {
            preloadScaleFactor++;
        }
        if (preloadScaleFactor > 1) {
            preloadScaleFactor--;//Min scaleFactor resulting in prescaled image larger than required
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = preloadScaleFactor;
            Bitmap prescaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);

            int width = prescaledBitmap.getWidth();
            int height = prescaledBitmap.getHeight();
            double scale = Math.min(maxSidePx / (double) width, maxSidePx / (double) height);
            int scaledWidth = (int) (width * scale);
            int scaledHeight = (int) (height * scale);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(prescaledBitmap, scaledWidth, scaledHeight, true);
            prescaledBitmap.recycle();
            File cacheDir = getImageResizeCacheDir(context);
            File downsizedImageFile = new File(cacheDir, "downsized_" + imageFile.getName());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(downsizedImageFile);
                fileOutputStream.write(byteArrayOutputStream.toByteArray());
                fileOutputStream.flush();
                fileOutputStream.close();
                scaledBitmap.recycle();
                return downsizedImageFile;
            } catch (IOException e) {
                ThreadsLogger.e(TAG, "downsizeImage", e);
                scaledBitmap.recycle();
                downsizedImageFile.delete();
                return null;
            }
        } else {
            return null;
        }

    }

    @Nullable
    public static Cursor getAllPhotos(@NonNull Context context) {
        String[] projection = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc";
        String selection = MediaStore.Images.Media.MIME_TYPE + " = ? OR " + MediaStore.Images.Media.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{
                "image/png",
                "image/jpeg"
        };
        return context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    private static File getImageResizeCacheDir(Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }

        File imageResizeCacheDir = new File(cacheDir.getAbsolutePath() + File.separator + IMAGE_RESIZE_CACHE_DIR_NAME);
        if (!imageResizeCacheDir.exists()) {
            imageResizeCacheDir.mkdir();
        }

        return imageResizeCacheDir;
    }
}
