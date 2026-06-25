package com.vpnsimple.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class V2RayVpnService : VpnService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isRunning = false
    private var v2rayProcess: Process? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val config = intent.getStringExtra(EXTRA_CONFIG) ?: return START_STICKY
                startVpnConnection(config)
            }
            ACTION_DISCONNECT -> {
                stopVpnConnection()
            }
        }
        return START_STICKY
    }

    private fun startVpnConnection(config: String) {
        if (isRunning) return
        
        try {
            val builder = Builder()
            builder.setSession("VPN Simple")
            builder.addAddress("10.0.0.2", 32)
            builder.addRoute("0.0.0.0", 0)
            builder.addDnsServer("1.1.1.1")
            builder.addDnsServer("8.8.8.8")
            builder.setMtu(1500)
            
            val vpnInterface = builder.establish()
            if (vpnInterface != null) {
                isRunning = true
                // V2Ray Core Integration Here
                // For now, mock implementation
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVpnConnection() {
        isRunning = false
        v2rayProcess?.destroy()
        v2rayProcess = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpnConnection()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_CONNECT = "com.vpnsimple.CONNECT"
        const val ACTION_DISCONNECT = "com.vpnsimple.DISCONNECT"
        const val EXTRA_CONFIG = "config"
    }
}
