package io.edna.threads.demo.iu.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.databinding.ActivityLaunchBinding

class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    checkLoginEnabled()
                }
            }
            checkLoginEnabled()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.login.isEnabled = false
        binding.login.setOnClickListener { login() }
        binding.serverButton.setOnClickListener { selectServer() }
        binding.userButton.setOnClickListener { selectUser() }
        binding.demonstrations.setOnClickListener { showDemonstration() }
        binding.settings.setOnClickListener { showSettings() }
        binding.about.text = generateAboutText()
    }

    private fun checkLoginEnabled() {
        binding.login.isEnabled = true
    }

    private fun login() {
    }

    private fun selectServer() {
        val intent = Intent(this, MainActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun selectUser() {
        val intent = Intent(this, MainActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun showDemonstration() {
        Toast.makeText(this, getString(R.string.functional_not_support), Toast.LENGTH_LONG).show()
    }

    private fun showSettings() {
        Toast.makeText(this, getString(R.string.functional_not_support), Toast.LENGTH_LONG).show()
    }

    private fun generateAboutText(): String {
        return "${getString(R.string.app_name)}  " +
            "v${BuildConfig.VERSION_NAME} " +
            "(${BuildConfig.VERSION_CODE})" +
            "/ ChatCenter SDK ${ThreadsLib.getLibVersion()}"
    }
}
