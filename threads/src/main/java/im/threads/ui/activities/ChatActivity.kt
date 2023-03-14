package im.threads.ui.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import im.threads.R
import im.threads.ui.fragments.ChatFragment

/**
 * Вся логика находится во фрагменте [ChatFragment]
 */
class ChatActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ecc_activity_chat_activity)
    }

    override fun onBackPressed() {
        (supportFragmentManager.findFragmentByTag("frag_chat") as? ChatFragment)?.let { chatFragment ->
            if (chatFragment.onBackPressed()) {
                super.onBackPressed()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == 123 && hasAllPermissionsGranted(grantResults)) {
            Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permissions not granted. Code: $requestCode", Toast.LENGTH_LONG).show()
        }
    }

    private fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }
}
