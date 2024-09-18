package moe.redbear.code_client

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class NginxService : Service() {
    private var nginxProcess: Process? = null
    private var stdoutThread: Thread? = null
    private var outReader: BufferedReader? = null
    private var stderrThread: Thread? = null
    private var errorReader: BufferedReader? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = Notification.Builder(this, "CHANNEL_ID")
            .setContentTitle("Nginx Running")
            .setContentText("Nginx for Code Client is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)

        try {
            val nginxDir = filesDir.absolutePath + "/nginx"

            val configStr = generateConfigStr(intent?.getStringExtra("hostUrl") ?: "");
            val configFile = File(nginxDir, "nginx.conf")
            configFile.writeText(configStr)

            val command = arrayOf(
                "/system/bin/sh", "-c",
                "LD_LIBRARY_PATH=$nginxDir/:\$LD_LIBRARY_PATH $nginxDir/nginx -c $nginxDir/nginx.conf -p $nginxDir/"
            )

            Log.d("command: ", command.joinToString(" "))

            nginxProcess = Runtime.getRuntime().exec(command)

            stdoutThread = Thread {
                try {
                    outReader =
                        BufferedReader(InputStreamReader(nginxProcess?.inputStream))
                    var inputLine: String?
                    while (outReader!!.readLine().also { inputLine = it } != null) {
                        Log.d("Nginx Output", inputLine!!)
                    }
                    outReader!!.close()
                } catch (e: IOException) {
                    // idk how to close it in a prettier way
                    Log.e("Nginx: ", "Error closing streams: ${e.message}")
                } catch (e: InterruptedException) {
                    Log.e("Nginx: ", "Thread interruption: ${e.message}")
                }
            }

            stderrThread = Thread {
                try {
                    errorReader =
                        BufferedReader(InputStreamReader(nginxProcess?.errorStream))
                    var errorLine: String?
                    while (errorReader!!.readLine().also { errorLine = it } != null) {
                        Log.e("Nginx Error", errorLine!!)
                    }
                    errorReader!!.close()
                } catch (e: IOException) {
                    // idk how to close it in a prettier way
                    Log.e("Nginx: ", "Error closing streams: ${e.message}")
                } catch (e: InterruptedException) {
                    Log.e("Nginx: ", "Thread interruption: ${e.message}")
                }
            }

            stdoutThread!!.start()
            stderrThread!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun generateConfigStr(hostUrl: String): String {
        val nginxDir = filesDir.absolutePath + "/nginx"
        return """
worker_processes auto;
worker_cpu_affinity auto;
pid $nginxDir/nginx.pid;
error_log $nginxDir/error.log;
daemon off;


events {
    worker_connections 1024;
}

http {
    client_body_temp_path $nginxDir/client-body;
    proxy_temp_path $nginxDir/proxy;
    fastcgi_temp_path $nginxDir/fastcgi;
    uwsgi_temp_path $nginxDir/uwsgi;
    scgi_temp_path $nginxDir/scgi;
    access_log $nginxDir/access.log;


    server {
        listen 8081;

        location / {
            proxy_pass ${hostUrl};
            proxy_set_header Host ${'$'}http_host;
            proxy_set_header Upgrade ${'$'}http_upgrade;
            proxy_set_header Connection upgrade;
            proxy_set_header Accept-Encoding gzip;
        }
    }
}
            """.trimIndent()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        val nginxDir = filesDir.absolutePath + "/nginx"
        val stopCommand = arrayOf(
            "/system/bin/sh", "-c",
            "LD_LIBRARY_PATH=$nginxDir/:\$LD_LIBRARY_PATH $nginxDir/nginx -s stop -c $nginxDir/nginx.conf -p $nginxDir/"
        )

        Log.d("Stop Nginx Command", stopCommand.joinToString(" "))
        val stopProcess = Runtime.getRuntime().exec(stopCommand)
        stopProcess.waitFor()
        Log.d("Nginx: ", "Service destroyed, nginx process terminated")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val nginxDir = filesDir.absolutePath + "/nginx"
        val stopCommand = arrayOf(
            "/system/bin/sh", "-c",
            "LD_LIBRARY_PATH=$nginxDir/:\$LD_LIBRARY_PATH $nginxDir/nginx -s stop -c $nginxDir/nginx.conf -p $nginxDir/"
        )

        Log.d("Stop Nginx Command", stopCommand.joinToString(" "))
        val stopProcess = Runtime.getRuntime().exec(stopCommand)
        stopProcess.waitFor()
        stopSelf()
        Log.d("Nginx: ", "Task removed, nginx process terminated")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "CHANNEL_ID",
                "Nginx Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
