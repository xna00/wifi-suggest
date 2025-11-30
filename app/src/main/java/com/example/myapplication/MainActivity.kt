package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 定义带启用状态的WiFi配置数据类
data class WifiConfigWithEnable(
    val ssid: String, val password: String, val enable: Boolean = true
)

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    // 通知权限请求注册
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i(TAG, "Notification permission granted")
        } else {
            Log.i(TAG, "Notification permission denied")
            Toast.makeText(this, "需要通知权限才能发送通知", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MyApplicationApp()
            }
        }
        // 检查并请求通知权限
        checkNotificationPermission()
        // 处理传入的Intent
        handleIntent(intent)
    }

    // 处理从文件管理器分享的文件
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEND == intent.action && intent.type != null) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (uri != null) {
                // 处理分享的文件URI
                Log.i(TAG, "Received shared file: $uri")
                Toast.makeText(this, "已接收分享文件，开始读取内容...", Toast.LENGTH_LONG).show()
                // 读取文件内容并发送POST请求
                readFileContentAndSendRequest(uri)
            }
        }
    }
    
    // 读取文件内容并发送POST请求
    private fun readFileContentAndSendRequest(fileUri: Uri) {
        val uploadUrl = "https://wifi-suggest.xna00.top/api/uploadbak"
        val queue = Volley.newRequestQueue(this)
        
        try {
            // 读取文件内容
            val inputStream = contentResolver.openInputStream(fileUri)
            val fileContent = inputStream?.bufferedReader().use { it?.readText() } ?: ""
            inputStream?.close()
            
            if (fileContent.isEmpty()) {
                Toast.makeText(this, "文件内容为空，无法上传", Toast.LENGTH_LONG).show()
                return
            }
            
            // 创建JSON请求体
            val jsonObject = org.json.JSONObject()
            jsonObject.put("content", fileContent)
            
            // 创建StringRequest发送POST请求
            val stringRequest = object : StringRequest(
                Request.Method.POST, uploadUrl,
                com.android.volley.Response.Listener { response ->
                    // 请求成功
                    Log.i(TAG, "Request successful: $response")
                    Toast.makeText(this, "文件内容发送成功", Toast.LENGTH_LONG).show()
                },
                com.android.volley.Response.ErrorListener { error ->
                    // 请求失败
                    Log.e(TAG, "Request failed: ${error.message}")
                    Toast.makeText(this, "文件内容发送失败: ${error.message}", Toast.LENGTH_LONG).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json"
                }
                
                @Throws(com.android.volley.AuthFailureError::class)
                override fun getBody(): ByteArray {
                    return jsonObject.toString().toByteArray(charset("UTF-8"))
                }
            }
            
            // 添加到请求队列
            queue.add(stringRequest)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing file: ${e.message}")
            Toast.makeText(this, "处理文件失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 当应用已经在运行时接收新的Intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    // 检查通知权限
    private fun checkNotificationPermission() {
        // Android 13及以上需要POST_NOTIFICATIONS权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 权限已授予
                Log.i(TAG, "Notification permission already granted")
            } else {
                // 请求权限
                Log.i(TAG, "Requesting notification permission")
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 13以下不需要动态请求通知权限
            Log.i(TAG, "Notification permission not required on this Android version")
        }
    }

    // 当Activity停止时（如用户返回到桌面），直接销毁应用
    override fun onStop() {
        super.onStop()
        Log.i(TAG, "Activity stopped, finishing app")
        finish() // 销毁当前Activity
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // URL状态管理
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("wifi_backup_prefs", Context.MODE_PRIVATE)
    val initialUrl = sharedPreferences.getString(
        "backup_url", "https://wifi-suggest.xna00.top/api/wifidata"
    ) ?: ""
    var url by rememberSaveable { mutableStateOf(initialUrl) }

    // 保存URL到SharedPreferences
    LaunchedEffect(url) {
        sharedPreferences.edit().putString("backup_url", url).apply()
    }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // URL多行输入框
                    TextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL") },
                        placeholder = { Text("请输入备份API地址") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        maxLines = 3,
                        singleLine = false,
                    )
                }
            }

        }) {
        Scaffold(
        ) { innerPadding ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Home(
                    modifier = Modifier.padding(innerPadding), url = url
                )

            } else {
                Text("仅支持Android 11及以上版本", modifier = Modifier.padding(innerPadding))
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun Home(
    modifier: Modifier = Modifier, url: String
) {
    val context = LocalContext.current
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    var connectedWifiSsid by remember { mutableStateOf(wifiInfo.ssid) }

    // 获取设备信息
    val deviceModel = Build.MODEL
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    Log.i("MyApplication", "当前连接WiFi: ${wifiInfo.ssid}, ${wifiInfo.supplicantState}")
    // 初始化SharedPreferences用于本地存储
    val sharedPreferences = context.getSharedPreferences("wifi_backup_prefs", Context.MODE_PRIVATE)
    val gson = Gson()

    // 从本地存储加载备份数据
    val savedBackupJson = sharedPreferences.getString("wifi_backup", null)
    val savedWifiList = if (savedBackupJson != null) {
        gson.fromJson(savedBackupJson, Array<WifiConfigWithEnable>::class.java).toList()
    } else {
        emptyList()
    }

    // 创建WiFi备份状态，包含带启用状态的WiFi列表
    var wifiBackupWithEnable by remember { mutableStateOf(savedWifiList) }
    val queue = Volley.newRequestQueue(context)

    // 保存备份数据到本地存储
    fun saveWifiBackup() {
        val backupJson = gson.toJson(wifiBackupWithEnable)
        sharedPreferences.edit().putString("wifi_backup", backupJson).apply()
        Log.i("MyApplication", "WiFi backup saved locally")
    }

    // 同步WiFi建议到系统
    fun syncWifiSuggestions() {
        val enabledWifis = wifiBackupWithEnable.filter { it.enable }
        val disabledWifis = wifiBackupWithEnable.filter { !it.enable }

        // 移除已禁用的WiFi建议
        if (disabledWifis.isNotEmpty()) {
            val suggestionsToRemove = wifiManager.networkSuggestions.filter { suggestion ->
                disabledWifis.any { it.ssid == suggestion.ssid }
            }
            if (suggestionsToRemove.isNotEmpty()) {
                wifiManager.removeNetworkSuggestions(suggestionsToRemove)
                Log.i(
                    "MyApplication", "Removed ${suggestionsToRemove.size} disabled WiFi suggestions"
                )
            }
        }

        // 添加已启用的WiFi建议
        if (enabledWifis.isNotEmpty()) {
            val suggestionsToAdd =
                makeSuggestions(enabledWifis.map { WifiConfig(it.ssid, it.password) })
            wifiManager.addNetworkSuggestions(suggestionsToAdd)
            Log.i("MyApplication", "Added ${suggestionsToAdd.size} enabled WiFi suggestions")
        }
    }

    LaunchedEffect(wifiBackupWithEnable) {
        // 将保存备份和同步建议的操作放到IO线程的coroutine中执行
        withContext(Dispatchers.IO) {
            saveWifiBackup()
            syncWifiSuggestions()
        }
    }

    fun fetchWifiBackup(url: String) {
        // 构建带查询参数的URL
        val urlWithParams = if (url.contains("?")) {
            "$url&model=$deviceModel&deviceId=$deviceId"
        } else {
            "$url?model=$deviceModel&deviceId=$deviceId"
        }

        val stringRequest = StringRequest(Request.Method.GET, urlWithParams, { response ->
            // 成功回调
            Log.i("MyApplication", "Response is: $response")
            val backup = gson.fromJson(response, WifiBackup::class.java)

            // 将获取的WiFi列表转换为带启用状态的列表
            // 如果本地有保存的启用状态，使用保存的，否则默认启用
            val newWifiList = backup.wifiList.map { newWifi ->
                val savedWifi = wifiBackupWithEnable.find { it.ssid == newWifi.ssid }
//                Log.i("MyApplication", "savedWifi: $savedWifi")
                WifiConfigWithEnable(
                    ssid = newWifi.ssid,
                    password = newWifi.password,
                    enable = savedWifi?.enable ?: true
                )
            }

            wifiBackupWithEnable = newWifiList
            Toast.makeText(
                context, "获取WiFi列表成功，数量：${newWifiList.size}", Toast.LENGTH_LONG
            ).show()
        }, {
            // 错误回调
            Log.e("MyApplication", "That didn't work!")
            Toast.makeText(context, "获取WiFi列表失败", Toast.LENGTH_LONG).show()
        })
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    // 应用初始化时，发起请求获取最新的WiFi列表
    LaunchedEffect(Unit) {
        fetchWifiBackup(url)
    }
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                Log.i("MyApplication", "更新WiFi列表")
                fetchWifiBackup(url)
            }) {
                Text("更新")
            }
            Button(onClick = {
                // 全部启用
                wifiBackupWithEnable = wifiBackupWithEnable.map { it.copy(enable = true) }
            }) {
                Text("全部启用")
            }
            Button(onClick = {
                // 全部禁用
                wifiBackupWithEnable = wifiBackupWithEnable.map { it.copy(enable = false) }
            }) {
                Text("全部禁用")
            }
        }

        LazyColumn {
            // 对WiFi列表进行排序，当前连接的WiFi排在最前面
            items(
                wifiBackupWithEnable.sortedByDescending { "\"" + it.ssid + "\"" == connectedWifiSsid },
                key = { wifiItem -> wifiItem.ssid }) { wifiItem ->
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(wifiItem.ssid)
                        Text(
                            text = wifiItem.password, fontSize = 14.sp, // 字号稍微小点
                            color = Color.Gray // 颜色稍微淡点
                        )
                    }
                    Switch(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        checked = wifiItem.enable,
                        onCheckedChange = { isEnabled ->
                            // 更新启用状态
                            wifiBackupWithEnable = wifiBackupWithEnable.map {
                                if (it.ssid == wifiItem.ssid) {
                                    it.copy(enable = isEnabled)
                                } else {
                                    it
                                }
                            }
                        })
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
fun makeSuggestions(wifiConfigList: List<WifiConfig>): List<WifiNetworkSuggestion> {
    return wifiConfigList.filter {
        it.password.isNotEmpty()
    }.map {
        listOf(
            WifiNetworkSuggestion.Builder().setIsAppInteractionRequired(true).setSsid(it.ssid)
                .setWpa2Passphrase(it.password).build(),
            WifiNetworkSuggestion.Builder().setIsAppInteractionRequired(true).setSsid(it.ssid)
                .setWpa3Passphrase(it.password).build()
        )
    }.flatten()

}

@RequiresApi(Build.VERSION_CODES.R)
fun getEnabledSuggestions(wifiManager: WifiManager): List<WifiNetworkSuggestion> {
    return wifiManager.networkSuggestions

}