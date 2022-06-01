package im.threads.android.ui.developer_options

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import im.threads.android.data.ServerName
import im.threads.android.databinding.ActivityDeveloperOptionsBinding
import im.threads.android.ui.EditTransportConfigDialog
import im.threads.android.ui.add_server_dialog.AddServerDialog
import im.threads.android.ui.add_server_dialog.AddServerDialogActions
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeveloperOptionsActivity : AppCompatActivity() {
    private val viewModel: DeveloperOptionsViewModel by viewModel()
    private lateinit var binding: ActivityDeveloperOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeveloperOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeForServersList()
        subscribeForServerChanges()
        subscribeForRestartApp()
        setOnClickForActivateServer()
        setOnClickForChangeServer()
        setOnClickForAddServer()
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

    private fun subscribeForServerChanges() = with(binding) {
        viewModel.serverChangedLiveData.observe(this@DeveloperOptionsActivity) {
            activateServerBtn.visibility = View.VISIBLE
        }
    }

    private fun subscribeForRestartApp() {
        viewModel.restartAppLiveData.observe(this) {
            ProcessPhoenix.triggerRebirth(this)
        }
    }

    private fun setOnClickForActivateServer() = with(binding) {
        activateServerBtn.setOnClickListener { viewModel.onActivateServerClicked() }
    }

    private fun setOnClickForChangeServer() = with(binding) {
        editCurrentServerBtn.setOnClickListener {
            EditTransportConfigDialog.open(this@DeveloperOptionsActivity)
        }
    }

    private fun setOnClickForAddServer() = with(binding) {
        addServerBtn.setOnClickListener {
            val onServerAddedAction = object : AddServerDialogActions {
                override fun onServerAdded() {
                    viewModel.fetchServerNames()
                }
            }
            AddServerDialog.open(this@DeveloperOptionsActivity, onServerAddedAction)
        }
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
