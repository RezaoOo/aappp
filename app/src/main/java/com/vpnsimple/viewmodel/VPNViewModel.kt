package com.vpnsimple.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vpnsimple.data.ConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VPNViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ConfigRepository(application)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _currentConfig = MutableStateFlow<ConfigRepository.V2RayConfig?>(null)
    val currentConfig: StateFlow<ConfigRepository.V2RayConfig?> = _currentConfig

    private val _configList = MutableStateFlow<List<ConfigRepository.V2RayConfig>>(emptyList())
    val configList: StateFlow<List<ConfigRepository.V2RayConfig>> = _configList

    private val _isPinging = MutableStateFlow(false)
    val isPinging: StateFlow<Boolean> = _isPinging

    init {
        loadConfigs()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            val configs = repository.loadConfigs()
            _configList.value = configs
        }
    }

    fun pingAllConfigs() {
        viewModelScope.launch {
            _isPinging.value = true
            val updatedConfigs = repository.pingAllConfigs(_configList.value)
            _configList.value = updatedConfigs
            _isPinging.value = false
        }
    }

    fun connectToOptimalServer() {
        viewModelScope.launch {
            _isPinging.value = true
            
            val updatedConfigs = repository.pingAllConfigs(_configList.value)
            _configList.value = updatedConfigs
            
            val bestConfig = updatedConfigs.minByOrNull { it.ping }
            if (bestConfig != null) {
                _currentConfig.value = bestConfig
                _isConnected.value = true
            }
            
            _isPinging.value = false
        }
    }

    fun selectConfig(config: ConfigRepository.V2RayConfig) {
        _currentConfig.value = config
        if (_isConnected.value) {
            viewModelScope.launch {
                disconnect()
                _currentConfig.value = config
                _isConnected.value = true
            }
        }
    }

    suspend fun disconnect() {
        _isConnected.value = false
    }
}
