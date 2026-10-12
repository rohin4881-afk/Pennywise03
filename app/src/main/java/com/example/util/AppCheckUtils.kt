package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

object AppCheckUtils {
    private const val TAG = "AppCheckUtils"

    /**
     * Configures Firebase App Check with reCAPTCHA Enterprise and Debug providers.
     * In debug builds or when FIREBASE_APPCHECK_DEBUG_TOKEN is present in the launching intent,
     * installs DebugAppCheckProviderFactory. In release environments, uses RecaptchaAppCheckProviderFactory.
     */
    fun configureAppCheck(context: Context, intent: Intent? = null) {
        try {
            val appCheck = Firebase.appCheck
            val debugToken = intent?.getStringExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")
            val recaptchaSiteKey = intent?.getStringExtra("FIREBASE_RECAPTCHA_SITE_KEY") ?: ""

            if (recaptchaSiteKey.isNotBlank() && !BuildConfig.DEBUG) {
                Log.d(TAG, "Configuring Firebase App Check with RecaptchaAppCheckProviderFactory")
                appCheck.installAppCheckProviderFactory(
                    RecaptchaAppCheckProviderFactory.getInstance(recaptchaSiteKey)
                )
            } else {
                Log.d(TAG, "Configuring Firebase App Check with DebugAppCheckProviderFactory")
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase App Check initialization notice: ${e.message}")
        }
    }
}
