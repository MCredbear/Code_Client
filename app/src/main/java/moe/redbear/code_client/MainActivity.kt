@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package moe.redbear.code_client

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // decompress nginx
        val targetDir = File(filesDir, "nginx")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
            copyAssetFolder(this, "nginx", targetDir.absolutePath)
            val nginxFile = File(filesDir, "nginx/nginx")
            nginxFile.setExecutable(true)
        }

        // get notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    114514
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            Settings.read(context)

            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen(navController) }
                composable(
                    "webview/{url}",
                    arguments = listOf(navArgument("url") { type = NavType.StringType })
                ) { backStackEntry ->
                    val host = Settings.hosts.find { host ->
                        host.name == backStackEntry.arguments?.getString("url")
                    }
                    if (host != null) {
                        WebViewScreen(navController, host)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 114514) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d("Permission", "Notification permission granted.")
            } else {
//                Log.d("Permission", "Notification permission denied.")
            }
        }
    }

    private fun copyAssetFolder(context: Context, assetDir: String, targetDir: String): Boolean {
        val assetManager: AssetManager = context.assets
        return try {
            copyAssetFile(assetManager, "nginx", "$targetDir/nginx")
            copyAssetFile(assetManager, "libz.so.1", "$targetDir/libz.so.1")
            copyAssetFile(assetManager, "libssl.so.3", "$targetDir/libssl.so.3")
            copyAssetFile(assetManager, "libpcre2-posix.so", "$targetDir/libpcre2-posix.so")
            copyAssetFile(assetManager, "libpcre2-8.so", "$targetDir/libpcre2-8.so")
            copyAssetFile(assetManager, "libpcre2-32.so", "$targetDir/libpcre2-32.so")
            copyAssetFile(assetManager, "libpcre2-16.so", "$targetDir/libpcre2-16.so")
            copyAssetFile(assetManager, "libcrypt.so", "$targetDir/libcrypt.so")
            copyAssetFile(assetManager, "libcrypto.so.3", "$targetDir/libcrypto.so.3")
            copyAssetFile(assetManager, "libandroid-glob.so", "$targetDir/libandroid-glob.so")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun copyAssetFile(assetManager: AssetManager, assetPath: String, targetPath: String) {
        val inputStream: InputStream = assetManager.open(assetPath)
        val outFile = File(targetPath)
        val outputStream: OutputStream = FileOutputStream(outFile)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.close()
    }
}


