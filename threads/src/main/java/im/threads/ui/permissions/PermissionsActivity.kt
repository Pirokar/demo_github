package im.threads.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import im.threads.R
import im.threads.business.logger.LoggerEdna.error

/**
 * Активити для разрешений
 */
class PermissionsActivity : AppCompatActivity() {
    private var requiresCheck = false
    private var permissions: Array<String> = arrayOf()
    private var textId = TEXT_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS) ?: arrayOf()
            textId = intent.getIntExtra(EXTRA_PERMISSION_TEXT, TEXT_DEFAULT)
        }
        if (permissions.isEmpty()) {
            permissions = savedInstanceState?.getStringArray(EXTRA_PERMISSIONS) ?: arrayOf()
            textId = savedInstanceState?.getInt(EXTRA_PERMISSION_TEXT) ?: TEXT_DEFAULT
        }
        if (permissions.isEmpty()) {
            finish()
        }
        setContentView(R.layout.ecc_activity_permissions)
        requiresCheck = true
    }

    override fun onResume() {
        super.onResume()
        if (requiresCheck && permissions.isNotEmpty()) {
            if (PermissionsChecker.permissionsDenied(this@PermissionsActivity, *permissions)) {
                requestPermissions(*permissions)
            } else {
                allPermissionsGranted()
            }
        } else if (permissions.isEmpty()) {
            showPermissionsIsNullLog()
            finish()
        } else {
            requiresCheck = true
        }
    }

    private fun allPermissionsGranted() {
        setResult(RESPONSE_GRANTED)
        finish()
    }

    private fun allPermissionsDenied() {
        setResult(RESPONSE_DENIED)
        finish()
    }

    private fun neverAskAgain() {
        setResult(RESPONSE_NEVER_AGAIN)
        finish()
    }

    private fun requestPermissions(vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(permissions, grantResults)) {
            requiresCheck = true
            allPermissionsGranted()
        } else {
            requiresCheck = false
            showMissingPermissionDialog()
        }
    }

    private fun hasAllPermissionsGranted(permissions: Array<String>, grantResults: IntArray): Boolean {
        val isApi34OrMore = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        val indexOfVisualUserPermission = if (isApi34OrMore) {
            permissions.indexOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        } else {
            -1
        }
        if (indexOfVisualUserPermission > 0 && grantResults[indexOfVisualUserPermission] == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        for (i in grantResults.indices) {
            val grantResult = grantResults[i]

            if (grantResult == PackageManager.PERMISSION_DENIED && i != indexOfVisualUserPermission) {
                return false
            }
        }
        return true
    }

    private fun showMissingPermissionDialog() {
        val dialogBuilder = AlertDialog.Builder(this@PermissionsActivity)
        dialogBuilder.setTitle(R.string.ecc_permissions_help)
        dialogBuilder.setMessage(permissionText)
        dialogBuilder.setNegativeButton(R.string.ecc_close) { dialog: DialogInterface?, which: Int ->
            if (PermissionsChecker.clickedNeverAskAgain(this@PermissionsActivity, *permissions)) {
                neverAskAgain()
            } else {
                allPermissionsDenied()
            }
        }
        dialogBuilder.setPositiveButton(R.string.ecc_permissions_settings) { _: DialogInterface?, _: Int -> startAppSettings() }
        dialogBuilder.setOnCancelListener { allPermissionsDenied() }
        val alertDialog = dialogBuilder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this@PermissionsActivity, android.R.color.black))
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this@PermissionsActivity, android.R.color.black))
        }
        alertDialog.show()
    }

    private val permissionText: Int
        get() {
            if (textId == TEXT_DEFAULT) {
                textId = R.string.ecc_permissions_string_help_text
            }
            return textId
        }

    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse(PACKAGE_URL_SCHEME + packageName)
        startActivity(intent)
    }

    companion object {
        const val RESPONSE_GRANTED = 10
        const val RESPONSE_DENIED = 20
        const val RESPONSE_NEVER_AGAIN = 30
        private const val PERMISSION_REQUEST_CODE = 0
        private const val EXTRA_PERMISSIONS = "EXTRA_PERMISSIONS"
        private const val EXTRA_PERMISSION_TEXT = "EXTRA_PERMISSION_TEXT"
        private const val PACKAGE_URL_SCHEME = "package:" // Для открытия настроек
        private const val TEXT_DEFAULT = -1

        @JvmStatic
        fun startActivityForResult(activity: Activity?, requestCode: Int, text: Int, vararg permissions: String) {
            if (permissions.isNotEmpty()) {
                val intent = Intent(activity, PermissionsActivity::class.java)
                intent.putExtra(EXTRA_PERMISSIONS, permissions)
                intent.putExtra(EXTRA_PERMISSION_TEXT, text)
                ActivityCompat.startActivityForResult(activity!!, intent, requestCode, null)
            } else {
                showPermissionsIsNullLog()
            }
        }

        private fun showPermissionsIsNullLog() {
            error("Permissions array is empty")
        }
    }
}
