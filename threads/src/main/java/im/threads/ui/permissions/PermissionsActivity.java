package im.threads.ui.permissions;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import im.threads.R;
import im.threads.business.logger.core.LoggerEdna;

/**
 * Активити для разрешений
 */
public final class PermissionsActivity extends AppCompatActivity {

    public static final int RESPONSE_GRANTED = 10;
    public static final int RESPONSE_DENIED = 20;
    public static final int RESPONSE_NEVER_AGAIN = 30;
    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String EXTRA_PERMISSIONS = "EXTRA_PERMISSIONS";
    private static final String EXTRA_PERMISSION_TEXT = "EXTRA_PERMISSION_TEXT";
    private static final String PACKAGE_URL_SCHEME = "package:";  // Для открытия настроек
    private static final int TEXT_DEFAULT = -1;
    private boolean requiresCheck;

    private String[] permissions;
    private int textId = TEXT_DEFAULT;

    /**
     * Запуск активити
     */
    public static void startActivityForResult(Activity activity, int requestCode, int text, String... permissions) {
        if (isPermissionsNonNull(permissions)) {
            Intent intent = new Intent(activity, PermissionsActivity.class);
            intent.putExtra(EXTRA_PERMISSIONS, permissions);
            intent.putExtra(EXTRA_PERMISSION_TEXT, text);
            ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
        } else {
            showPermissionsIsNullLog();
        }
    }

    public static void startActivityForResult(Fragment fragment, int requestCode, int text, String... permissions) {
        if (isPermissionsNonNull(permissions)) {
            Intent intent = new Intent(fragment.getActivity(), PermissionsActivity.class);
            intent.putExtra(EXTRA_PERMISSIONS, permissions);
            intent.putExtra(EXTRA_PERMISSION_TEXT, text);
            fragment.startActivityForResult(intent, requestCode, null);
        } else {
            showPermissionsIsNullLog();
        }
    }

    /**
     * Запуск активити
     */
    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        startActivityForResult(activity, requestCode, TEXT_DEFAULT, permissions);
    }

    private static boolean isPermissionsNonNull(String[] permissions) {
        if (permissions == null) return false;
        boolean isNull = false;
        for (String permission : permissions) {
            if (permission == null) {
                isNull = true;
                break;
            }
        }
        return !isNull;
    }

    private static void showPermissionsIsNullLog() {
        LoggerEdna.error("Permissions array is null");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            permissions = getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
            textId = getIntent().getIntExtra(EXTRA_PERMISSION_TEXT, TEXT_DEFAULT);
        }

        if (permissions == null) {
            permissions = savedInstanceState.getStringArray(EXTRA_PERMISSIONS);
            textId = savedInstanceState.getInt(EXTRA_PERMISSION_TEXT);
        }

        if (permissions == null) {
            finish();
        }
        setContentView(R.layout.activity_permissions);
        requiresCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requiresCheck && isPermissionsNonNull(permissions)) {
            // Если разрешения не выданы, то нужно открыть диалог с разрешениями
            if (PermissionsChecker.permissionsDenied(PermissionsActivity.this, permissions)) {
                requestPermissions(permissions);
            } else {
                // Если выданы, закрыть активити с положительным результатом
                allPermissionsGranted();
            }
        } else if (permissions == null) {
            showPermissionsIsNullLog();
            finish();
        } else {
            requiresCheck = true;
        }
    }

    private void allPermissionsGranted() {
        setResult(RESPONSE_GRANTED);
        finish();
    }

    private void allPermissionsDenied() {
        setResult(RESPONSE_DENIED);
        finish();
    }

    private void neverAskAgain() {
        setResult(RESPONSE_NEVER_AGAIN);
        finish();
    }

    /**
     * Запуск диалога с разрешениями
     */
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            // Если пользователь разрешил все, закрыть экран с результатом ОК
            requiresCheck = true;
            allPermissionsGranted();
        } else {
            requiresCheck = false;
            // Если что-то отклонено - показать объясняющий текст
            showMissingPermissionDialog();
        }
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void showMissingPermissionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PermissionsActivity.this);
        dialogBuilder.setTitle(R.string.threads_permissions_help);
        dialogBuilder.setMessage(getPermissionText());
        dialogBuilder.setNegativeButton(R.string.threads_close, (dialog, which) -> {
            if (PermissionsChecker.clickedNeverAskAgain(PermissionsActivity.this, permissions)) {
                // Если во всех кликнуто - БОЛЬШЕ НЕ ПОКАЗЫВАТЬ, то закрыть с соответствующим результатом
                neverAskAgain();
            } else {
                // Если все запретили, то закрыть экран с соответствующим результатом
                allPermissionsDenied();
            }
        });
        dialogBuilder.setPositiveButton(R.string.threads_permissions_settings, (dialog, which) -> startAppSettings());
        dialogBuilder.setOnCancelListener(dialog -> allPermissionsDenied());
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PermissionsActivity.this, android.R.color.black));
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PermissionsActivity.this, android.R.color.black));
        });
        alertDialog.show();
    }

    private int getPermissionText() {
        if (textId == TEXT_DEFAULT) {
            textId = R.string.threads_permissions_string_help_text;
        }
        return textId;
    }

    private void startAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }
}

