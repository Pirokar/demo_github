package im.threads.android.ui.developer_options

import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import im.threads.android.data.ServerName
import im.threads.android.databinding.ActivityDeveloperOptionsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeveloperOptionsActivity : AppCompatActivity() {
    private val viewModel: DeveloperOptionsViewModel by viewModel()
    private lateinit var binding: ActivityDeveloperOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeveloperOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeForServersList()
        viewModel.fetchServerNames()
    }

    private fun setOnServerRBClick() = with(binding) {
        serversRadioButtons.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onCheckedChange(checkedId)
        }
    }

    private fun subscribeForServersList() {
        viewModel.serverNamesLiveData.observe(this) { fillServerListView(it) }
    }

    private fun fillServerListView(serverList: List<ServerName>) = with(binding) {
        serversRadioButtons.removeAllViews()
        serverList.forEach { serverName ->
            val radioButton = RadioButton(baseContext).apply {
                text = serverName.name
                id = viewModel.getIdForString(serverName.name)
                isChecked = serverName.isSelected
            }
            serversRadioButtons.addView(radioButton)
        }
        setOnServerRBClick()
    }
}
