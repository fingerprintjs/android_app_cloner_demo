package com.fingerprint.myapplication
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.fingerprintjs.android.fpjs_pro.Configuration
import com.fingerprintjs.android.fpjs_pro.FingerprintJSFactory


class MainActivity : AppCompatActivity() {

    // Get a 'visitorId'
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize and configure the SDK
        val factory = FingerprintJSFactory(applicationContext)
        val configuration = Configuration(
            // public API key from Fingerprint Dashboard
            apiKey = "PUBLIC_API_KEY",
            region = Configuration.Region.US
        )
        val fpjsClient = factory.createInstance(configuration)
        val emailField = findViewById<EditText>(R.id.emailEditText)
        val passwordField = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            fpjsClient.getVisitorId { visitorIdResponse ->
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()
                val requestId = visitorIdResponse.requestId
                makePostRequest(email, password, requestId)
            }
        }
    }

    private fun makePostRequest(email: String, password: String, requestId: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("requestId", requestId)
        }.toString()

        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:3000")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                runOnUiThread {
                    if (response.isSuccessful) {

                        Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Login failed: $responseBody",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
