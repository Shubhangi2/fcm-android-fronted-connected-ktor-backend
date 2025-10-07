package com.example.fcm_learning

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fcm_learning.ui.components.ChatScreen
import com.example.fcm_learning.ui.components.EnterTokenDialog
import com.example.fcm_learning.ui.theme.Fcm_learningTheme

class MainActivity : ComponentActivity() {
    private val viewModel : ChatViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            Fcm_learningTheme {
                Surface (
                    color = MaterialTheme.colorScheme.background,
                     modifier = Modifier.fillMaxSize()
                ){
                    val state = viewModel.state;
                    if(state.isEnteringToken){
                        EnterTokenDialog(
                            token = state.remoteToken,
                            onTokenChanged = viewModel::onRemoteTokenChange,
                            onSubmit = viewModel::onSubmitRemoteToken
                        )
                    }else{
                        ChatScreen(
                            messageText = state.messageText,
                            onMessageSend = {
                                viewModel.sendMessage(isBoradcast = false)
                            },
                            onMessageBroadcast = {
                                viewModel.sendMessage(isBoradcast = false)
                            },
                            onMessageChanged = viewModel::onMessagechange
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val hasPermssion = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if(!hasPermssion){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
    }
}

