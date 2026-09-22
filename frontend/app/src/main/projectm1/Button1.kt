package com.example.projectm1

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.*

@Composable
fun Button1Screen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var status by remember { mutableStateOf("Not logged in - tap Login") }
    var isLoggedIn by remember { mutableStateOf(false) }
    var googleUserName by remember { mutableStateOf("") }
    var googleEmail by remember { mutableStateOf("") }
    var serverPublicIp by remember { mutableStateOf("-") }
    var clientIp by remember { mutableStateOf("-") }
    var serverTime by remember { mutableStateOf("-") }
    var clientTime by remember { mutableStateOf("-") }
    var backendName by remember { mutableStateOf("-") }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            status = "Cancelled / resultCode=${result.resultCode} - Try real phone + Google account logged in"
            return@rememberLauncherForActivityResult
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            googleUserName = account.displayName ?: "${account.givenName ?: ""} ${account.familyName ?: ""}".trim()
            googleEmail = account.email ?: ""
            if (googleUserName.isEmpty()) googleUserName = "Google User"
            status = "Login SUCCESS as $googleUserName ($googleEmail)"
            isLoggedIn = true
            Log.d("LOGIN", "SUCCESS name=$googleUserName email=$googleEmail")
        } catch (e: ApiException) {
            status = "Google FAILED: Code=${e.statusCode}\n10=SHA1/json mismatch, 12500=Enable Google in Firebase Auth"
            Log.e("LOGIN", "FAILED code=${e.statusCode}", e)
            isLoggedIn = false
        }
    }

    fun getUnsafeClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(c: Array<X509Certificate>, a: String) {}
            override fun checkServerTrusted(c: Array<X509Certificate>, a: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAll, SecureRandom())
        return OkHttpClient.Builder()
            .sslSocketFactory(sc.socketFactory, trustAll[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val BASE_URL = "https://20.43.80.147:3000"
                if (BASE_URL.contains("YOUR_VM_IP")) {
                    status = "Login SUCCESS as $googleUserName ($googleEmail) - Backend not set yet"
                    return@withContext
                }
                val client = getUnsafeClient()

                serverPublicIp = JSONObject(client.newCall(Request.Builder().url("$BASE_URL/api/server-ip").build()).execute().body?.string() ?: "{}").optString("ip")
                clientIp = JSONObject(client.newCall(Request.Builder().url("$BASE_URL/api/client-ip").build()).execute().body?.string() ?: "{}").optString("ip")
                serverTime = JSONObject(client.newCall(Request.Builder().url("$BASE_URL/api/server-time").build()).execute().body?.string() ?: "{}").optString("time")
                val nameJson = JSONObject(client.newCall(Request.Builder().url("$BASE_URL/api/my-name").build()).execute().body?.string() ?: "{}")
                backendName = "${nameJson.optString("firstName")} ${nameJson.optString("lastName")}"

                // Client local time - 24hr GMT format: hh:mm:ss GMT+hh:mm
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
                val now = Date()
                val offset = TimeZone.getDefault().getOffset(now.time)
                val sign = if (offset >= 0) "+" else "-"
                val offsetStr = String.format(Locale.US, "%02d:%02d", Math.abs(offset/3600000), Math.abs((offset/60000)%60))
                clientTime = "${sdf.format(now)} GMT$sign$offsetStr"

                status = "All HTTPS APIs loaded!"
            } catch (e: Exception) {
                status = "Logged in as $googleUserName but backend error: ${e.message}"
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("← Back") }

        // Green when loaded, red otherwise
        val statusColor = if (status.contains("All HTTPS APIs loaded")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
        Text(status, modifier = Modifier.padding(vertical = 12.dp), color = statusColor)

        Button(onClick = {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val gClient = GoogleSignIn.getClient(context, gso)
            gClient.signOut()
            launcher.launch(gClient.signInIntent)
        }, Modifier.fillMaxWidth().height(56.dp)) {
            Text("Login with Google")
        }

        if (isLoggedIn) {
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Server public IP address", serverPublicIp)
                    InfoRow("Client IP address", clientIp)
                    InfoRow("Server local time", serverTime)
                    InfoRow("Client local time", clientTime)
                    InfoRow("Your name", backendName)
                    InfoRow("Google Account Name", googleUserName)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
        Divider(Modifier.padding(top = 4.dp))
    }
}