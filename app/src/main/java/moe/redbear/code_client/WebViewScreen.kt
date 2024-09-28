package moe.redbear.code_client

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.view.View
import android.webkit.JavascriptInterface

@OptIn(DelicateCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WebViewScreen(navController: NavController, host: Host) {
    val context = LocalContext.current

    LaunchedEffect(host.url) {
        val serviceIntent = Intent(context, NginxService::class.java).apply {
            putExtra("hostUrl", host.url)
        }
        context.startForegroundService(serviceIntent)
    }

    DisposableEffect(host.url) {
        onDispose {
            val serviceIntent = Intent(context, NginxService::class.java)
            context.stopService(serviceIntent)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { context ->
            WebView.setWebContentsDebuggingEnabled(true)
            CustomWebView(context).apply {
                addJavascriptInterface(CustomClipboardInterface(context), "AndroidClipboard")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            """
                            navigator.clipboard.readText = function() {
                                return new Promise(function(resolve, reject) {
                                    try {
                                        var clipboardText = AndroidClipboard.getClipboardText();
                                        resolve(clipboardText);
                                    } catch (error) {
                                        reject(error);
                                    }
                                });
                            };
        """.trimIndent(), null
                        )
                    }
                }
                settings.apply {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    javaScriptEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    domStorageEnabled = true
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    loadsImagesAutomatically = true
                    allowFileAccess = true
                    allowContentAccess = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }

                GlobalScope.launch(Dispatchers.Main) {
                    delay(1000)
                    loadUrl("localhost:8081")
                }
            }
        }, modifier = Modifier.fillMaxSize())
    }
}

class CustomWebView(context: Context) : WebView(context) {
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event != null) {
            if (event.action == ACTION_DOWN)
                when (event.keyCode) {
                    KeyEvent.KEYCODE_FORWARD -> {
                        evaluateJavascript(
                            "document.getElementsByClassName('content')[0].dispatchEvent(new MouseEvent('mousedown', {button: 4, buttons: 16, bubbles: true, view: window}));",
                            null
                        )
                        Log.d("CustomWebView", "Forward mouse button clicked")
                        return true
                    }

                    KeyEvent.KEYCODE_BACK -> {
                        evaluateJavascript(
                            "document.getElementsByClassName('content')[0].dispatchEvent(new MouseEvent('mousedown', {button: 3, buttons: 8, bubbles: true, view: window}));",
                            null
                        )
                        Log.d("CustomWebView", "Back mouse button clicked")
                        return true
                    }
                }
        }
        return super.dispatchKeyEvent(event)
    }
}

class CustomClipboardInterface(private val context: Context) {
    @JavascriptInterface
    fun getClipboardText(): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            return item?.text?.toString() ?: ""
        }
        return ""
    }
}
