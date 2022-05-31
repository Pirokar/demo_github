package im.threads.android.ui.developer_options

import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeveloperOptionsActivity : AppCompatActivity() {
    private val viewModel: DeveloperOptionsVM by viewModel()
}
