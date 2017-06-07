package im.threads.permissions;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import im.threads.R;

/**
 * Created by chybakut2004 on 07.06.17.
 *
 *  * Активити для разрешений
 */

public class PermissionsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String EXTRA_PERMISSIONS = "EXTRA_PERMISSIONS";   // Ключ для передачи разрешений
    private static final String EXTRA_PERMISSION_TEXT = "EXTRA_PERMISSION_TEXT";   // Ключ для передачи разрешений
    private static final String PACKAGE_URL_SCHEME = "package:";           // Для открытия настроек

    public static final int TEXT_DEFAULT = -1;

    public static final int RESPONSE_GRANTED = 10;
    public static final int RESPONSE_DENIED = 20;
    public static final int RESPONSE_NEVER_AGAIN = 30;

    private PermissionsChecker checker; // Менеджер проверки резрешений
    private boolean requiresCheck;

    /**
     * Запуск активити
     */
    public static void startActivityForResult(Activity activity, int requestCode, int text, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        intent.putExtra(EXTRA_PERMISSION_TEXT, text);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    public static void startActivityForResult(Fragment fragment, int requestCode, int text, String... permissions) {
        Intent intent = new Intent(fragment.getActivity(), PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        intent.putExtra(EXTRA_PERMISSION_TEXT, text);
        fragment.startActivityForResult(intent, requestCode, null);
    }

    /**
     * Запуск активити
     */
    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        startActivityForResult(activity, requestCode, TEXT_DEFAULT, permissions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.");
        }
        setContentView(R.layout.activity_permissions);

        checker = new PermissionsChecker(this);
        requiresCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (requiresCheck) {
            String[] permissions = getPermissions();

            // Если разрешения не выданы, то нужно открыть диалог с разрешениями
            if (checker.permissionsDenied(permissions)) {
                requestPermissions(permissions);
            } else {
                // Если выданы, закрыть активити с положительным результатом
                allPermissionsGranted();
            }
        } else {
            requiresCheck = true;
        }
    }

    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
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
        dialogBuilder.setTitle(R.string.permissions_help);
        dialogBuilder.setMessage(getPermissionText());
        dialogBuilder.setNegativeButton(R.string.permissions_quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(checker.clickedNeverAskAgain(PermissionsActivity.this, getPermissions())) {
                    // Если во всех кликнуто - БОЛЬШЕ НЕ ПОКАЗЫВАТЬ, то закрыть с соответствующим результатом
                    neverAskAgain();
                } else {
                    // Если все запретили, то закрыть экран с соответствующим результатом
                    allPermissionsDenied();
                }
            }
        });
        dialogBuilder.setPositiveButton(R.string.permissions_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allPermissionsDenied();
            }
        });
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PermissionsActivity.this, android.R.color.black));
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PermissionsActivity.this, android.R.color.black));
            }
        });
        alertDialog.show();
    }

    private int getPermissionText() {
        int textId = getIntent().getIntExtra(EXTRA_PERMISSION_TEXT, TEXT_DEFAULT);

        if(textId == TEXT_DEFAULT) {
            textId = R.string.permissions_string_help_text;
        }

        return textId;
    }

    private void startAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }
}

