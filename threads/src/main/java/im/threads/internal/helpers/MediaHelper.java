package im.threads.internal.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public final class MediaHelper {

    public static void grantPermissionsForUri(final Context context, final Intent intent, final Uri imageUri) {
        final List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (!resInfoList.isEmpty()) {
            for (final ResolveInfo resolveInfo : resInfoList) {
                final String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    @Nullable
    public static Cursor getAllPhotos(@NonNull Context context) {
        String[] projection = new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc";
        String selection = MediaStore.Images.Media.MIME_TYPE + " = ? OR " + MediaStore.Images.Media.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{
                "image/png",
                "image/jpeg"
        };
        return context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }
}
