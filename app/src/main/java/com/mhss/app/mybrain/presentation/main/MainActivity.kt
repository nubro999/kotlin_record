package com.mhss.app.mybrain.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.ui.ThemeSettings
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {


    private val viewModel: MainViewModel by viewModel()

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 100
        private const val AUDIO_PERMISSION_CODE = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNotificationPermissionGranted())
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        if (!isAudioPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                AUDIO_PERMISSION_CODE
            )
        }
        val appLockManager = AppLockManager(this)
        setContent {
            val blockScreenshots by viewModel.blockScreenshots.collectAsState(initial = false)
            val isSystemDarkMode = isSystemInDarkTheme()
            val isDarkMode by viewModel.themeMode
                .map {
                    it == ThemeSettings.DARK.value || (it == ThemeSettings.AUTO.value && isSystemDarkMode)
                }.collectAsState(true)

            LaunchedEffect(blockScreenshots) {
                if (blockScreenshots) {
                    window.setFlags(
                        LayoutParams.FLAG_SECURE,
                        LayoutParams.FLAG_SECURE
                    )
                } else
                    window.clearFlags(LayoutParams.FLAG_SECURE)
            }
            LaunchedEffect(isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb(),
                        detectDarkMode = {
                            isDarkMode
                        }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb(),
                        detectDarkMode = {
                            isDarkMode
                        }
                    ),
                )
            }
            MyBrainApp(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                appLockManager = appLockManager
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 알림 권한 승인됨
                }
            }
            AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 마이크 권한 승인됨
                } else {
                    // 마이크 권한 거부됨 - 사용자에게 알림 표시
                    showAudioPermissionDeniedMessage()
                }
            }
        }
    }

    private fun isAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private fun showAudioPermissionDeniedMessage() {
    android.app.AlertDialog.Builder(MainActivity())
        .setTitle("마이크 권한 필요")
        .setMessage("음성 인식 기능을 사용하려면 마이크 권한이 필요합니다. 설정에서 권한을 허용해주세요.")
        .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
        .show()
}