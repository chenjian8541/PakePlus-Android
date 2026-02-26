package com.app.pakeplus

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val REQUEST_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        initWebView()
        requestRuntimePermissions()
    }

    private fun initWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.mediaPlaybackRequiresUserGesture = false

        webView.webChromeClient = object : WebChromeClient() {

            // 允许 H5 摄像头 / 麦克风
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    request.grant(request.resources)
                }
            }

            // 处理 input type=file
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                val intent = fileChooserParams?.createIntent()
                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    return false
                }
                return true
            }
        }

        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://你的H5地址") // 修改为你的地址
    }

    // 文件选择回调
    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (filePathCallback == null) return@registerForActivityResult

            val uris: Array<Uri>? = if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    WebChromeClient.FileChooserParams.parseResult(
                        result.resultCode,
                        data
                    )
                }
            } else null

            filePathCallback?.onReceiveValue(uris)
            filePathCallback = null
        }

    private fun requestRuntimePermissions() {
        val permissions = mutableListOf<String>()

        if (check(Manifest.permission.CAMERA))
            permissions.add(Manifest.permission.CAMERA)

        if (check(Manifest.permission.RECORD_AUDIO))
            permissions.add(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (check(Manifest.permission.READ_MEDIA_IMAGES))
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            if (check(Manifest.permission.READ_MEDIA_VIDEO))
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            if (check(Manifest.permission.READ_EXTERNAL_STORAGE))
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (check(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                REQUEST_PERMISSION
            )
        }
    }

    private fun check(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }
}
