package moe.redbear.code_client

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object Settings {
    val hosts = mutableStateListOf<Host>()

    fun save(context: Context) {
        val gson = Gson()
        val jsonString = gson.toJson(hosts)

        val file = File(context.filesDir, "hosts.json")
        FileWriter(file).use { writer ->
            writer.write(jsonString)
        }
    }

    fun read(context: Context) {
        val file = File(context.filesDir, "hosts.json")
        if (file.exists()) {
            FileReader(file).use { reader ->
                val gson = Gson()
                val type = object : TypeToken<List<Host>>() {}.type
                val loadedHosts: List<Host> = gson.fromJson(reader, type)
                hosts.clear()
                hosts.addAll(loadedHosts)
            }
        }
    }
}