package im.threads.internal.utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.FileDescription;

public final class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    private static final int JPEG = 0;
    private static final int PNG = 1;
    private static final int PDF = 2;
    private static final int OTHER_DOC_FORMATS = 3;
    private static final int UNKNOWN = -1;

    private static final String UNKNOWN_MIME_TYPE = "*/*";

    private FileUtils() {
    }

    @NonNull
    public static String getFileName(@NonNull FileDescription fd) {
        if (fd.getIncomingName() != null) {
            return fd.getIncomingName();
        } else if (fd.getFileUri() != null) {
            return FileUtils.getFileName(fd.getFileUri());
        }
        return "";
    }

    @NonNull
    public static String getFileName(@NonNull Uri uri) {
        try (Cursor cursor = Config.instance.context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        return "threads" + UUID.randomUUID();
    }

    public static long getFileSize(@NonNull Uri uri) {
        try (Cursor cursor = Config.instance.context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        return 0;
    }

    public static boolean isSupportedFile(@Nullable final FileDescription fileDescription) {
        return FileUtils.isImage(fileDescription) || FileUtils.isDoc(fileDescription);
    }

    public static boolean isImage(@Nullable final FileDescription fileDescription) {
        return fileDescription != null
                && (getExtensionFromFileDescription(fileDescription) == JPEG
                || getExtensionFromFileDescription(fileDescription) == PNG);
    }

    public static boolean isDoc(@Nullable final FileDescription fileDescription) {
        return fileDescription != null
                && (getExtensionFromFileDescription(fileDescription) == PDF
                || getExtensionFromFileDescription(fileDescription) == OTHER_DOC_FORMATS);
    }

    @NonNull
    public static String getMimeType(@NonNull FileDescription fd) {
        if (!TextUtils.isEmpty(fd.getMimeType())) {
            return fd.getMimeType();
        }
        if (fd.getFileUri() != null) {
            return getMimeType(fd.getFileUri());
        }
        return UNKNOWN_MIME_TYPE;
    }

    @NonNull
    public static String getMimeType(@NonNull Uri uri) {
        Context context = Config.instance.context;
        String type = context.getContentResolver().getType(uri);
        if (type == null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        }
        return type != null ? type : UNKNOWN_MIME_TYPE;
    }

    public static Uri safeParse(@Nullable final String source) {
        return (source != null) ? Uri.parse(source) : null;
    }

    public static void saveToDownloads(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = Config.instance.context.getContentResolver();
            ContentValues imageCV = new ContentValues();
            imageCV.put(MediaStore.Images.Media.DISPLAY_NAME, getFileName(uri));
            imageCV.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(uri));
            Uri imagesCollection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri outputUri = resolver.insert(imagesCollection, imageCV);
            if (outputUri == null) {
                imageCV.put(MediaStore.Images.Media.DISPLAY_NAME, "threads" + UUID.randomUUID());
                outputUri = resolver.insert(imagesCollection, imageCV);
            }
            saveToUri(uri, outputUri);
        } else {
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getFileName(uri));
            if (outputFile.exists() || outputFile.createNewFile()) {
                saveToFile(uri, outputFile);
                DownloadManager dm = (DownloadManager) Config.instance.context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.addCompletedDownload(
                            getFileName(uri),
                            Config.instance.context.getString(R.string.threads_media_description),
                            true,
                            getMimeType(uri),
                            outputFile.getPath(),
                            outputFile.length(),
                            false
                    );
                }
            } else {
                throw new FileNotFoundException();
            }
        }
    }

    public static String convertRelativeUrlToAbsolute(@Nullable String relativeUrl) {
        if (TextUtils.isEmpty(relativeUrl) || relativeUrl.startsWith("http")) {
            return relativeUrl;
        }
        return MetaDataUtils.getDatastoreUrl(Config.instance.context) + "files/" + relativeUrl;
    }

    private static int getExtensionFromFileDescription(@NonNull FileDescription fileDescription) {
        String mimeType = getMimeType(fileDescription);
        if (!mimeType.equals(UNKNOWN_MIME_TYPE)) {
            return getExtensionFromMimeType(mimeType);
        }
        return getExtensionFromPath(fileDescription.getIncomingName());
    }

    public static int getExtensionFromPath(@Nullable String path) {
        if (path == null || !path.contains(".")) {
            return UNKNOWN;
        }
        String extension = path.substring(path.lastIndexOf(".") + 1);
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            return JPEG;
        }
        if (extension.equalsIgnoreCase("png")) {
            return PNG;
        }
        if (extension.equalsIgnoreCase("pdf")) {
            return PDF;
        }
        if (extension.equalsIgnoreCase("txt")
                || extension.equalsIgnoreCase("doc")
                || extension.equalsIgnoreCase("docx")
                || extension.equalsIgnoreCase("xls")
                || extension.equalsIgnoreCase("xlsx")
                || extension.equalsIgnoreCase("xlsm")
                || extension.equalsIgnoreCase("xltx")
                || extension.equalsIgnoreCase("xlt")) {
            return OTHER_DOC_FORMATS;
        }
        return UNKNOWN;
    }

    private static int getExtensionFromMimeType(@NonNull String mimeType) {
        switch (mimeType) {
            case "image/jpeg":
                return JPEG;
            case "image/png":
                return PNG;
            case "application/pdf":
                return PDF;
            case "text/plain":
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/vnd.ms-excel.sheet.macroenabled.12":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.template":
            default:
                return UNKNOWN;
        }
    }

    private static void saveToFile(Uri uri, File outputFile) throws IOException {
        Bitmap bitmap = Picasso.get()
                .load(uri)
                .get();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
            bitmap.recycle();
        } catch (IOException e) {
            ThreadsLogger.e(TAG, "saveToFile", e);
            bitmap.recycle();
        }
    }

    private static void saveToUri(Uri uri, Uri outputUri) throws IOException {
        ContentResolver resolver = Config.instance.context.getContentResolver();
        Bitmap bitmap = Picasso.get()
                .load(uri)
                .get();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        try (OutputStream fileOutputStream = resolver.openOutputStream(outputUri)) {
            if (fileOutputStream != null) {
                fileOutputStream.write(byteArrayOutputStream.toByteArray());
                fileOutputStream.flush();
                bitmap.recycle();
            }
        } catch (IOException e) {
            ThreadsLogger.e(TAG, "saveToUri", e);
            bitmap.recycle();
        }
    }

    public static String getExtensionFromMediaStore(Context context, Uri contentUri) {
        if (contentUri == null || context == null) {
            return null;
        }
        String path = getFilePathFromMediaStore(context, contentUri);
        if (path == null || !path.contains(".")) {
            return null;
        }
        return path.substring(path.lastIndexOf(".") + 1);
    }

    @SuppressLint("NewApi")
    public static String getFilePathFromMediaStore(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @SuppressLint("NewApi")
    public static long getFileSizeFromMediaStore(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return new File(Environment.getExternalStorageDirectory() + "/" + split[1]).length();
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return new File(uri.getLastPathSegment()).length();
            }

            String[] projection = {
                    MediaStore.Images.Media.SIZE
            };
            Cursor cursor;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                if (cursor.moveToFirst()) {
                    return cursor.getLong(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return new File(uri.getPath()).length();
        }
        return 0;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
