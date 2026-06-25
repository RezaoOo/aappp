package com.vpnsimple.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import kotlin.math.roundToInt

class ConfigRepository(private val context: Context) {

    data class V2RayConfig(
        val name: String,
        val uri: String,
        var ping: Int = 9999
    )

    suspend fun loadConfigs(): List<V2RayConfig> = withContext(Dispatchers.IO) {
        return@withContext try {
            val json = context.assets.open("v2ray_configs.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<V2RayConfig>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: IOException) {
            emptyList()
        }
    }

    suspend fun pingConfig(config: V2RayConfig): Int = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val startTime = System.currentTimeMillis()
            connection.getInputStream().close()
            val endTime = System.currentTimeMillis()
            
            ((endTime - startTime) / 1.0).roundToInt()
        } catch (e: Exception) {
            9999
        }
    }

    suspend fun pingAllConfigs(configs: List<V2RayConfig>): List<V2RayConfig> = withContext(Dispatchers.IO) {
        return@withContext configs.map { config ->
            config.copy(ping = pingConfig(config))
        }
    }
}
