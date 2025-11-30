package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class WifiBackup(
    @SerializedName("wifiList") val wifiList: List<WifiConfig>
)

data class WifiConfig(
    @SerializedName("ssid") val ssid: String,          // WiFi名称
    @SerializedName("password") val password: String,  // 密码（OPEN类型为空）
//    @SerializedName("securityType") val securityType: String  // 加密类型
)
