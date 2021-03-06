package br.com.heiderlopes.calculaflex

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*
import android.content.SharedPreferences
import br.com.heiderlopes.calculaflex.utils.RemoteConfig
import android.content.DialogInterface
import android.net.Uri
import android.support.v7.app.AlertDialog

class SplashActivity : BaseActivity() {

    private val TEMPO_AGUARDO_SPLASHSCREEN = 3500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        RemoteConfig.remoteConfigFetch()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    RemoteConfig.getFirebaseRemoteConfig().activateFetched()
                    val minVersionApp = RemoteConfig.getFirebaseRemoteConfig()
                        .getLong("min_version_app")
                        .toInt()
                    if (minVersionApp <= BuildConfig.VERSION_CODE)
                        continueApp()
                    else
                        showAlertMinVersion()
                } else
                    continueApp()
            }
    }

    private fun continueApp() {
        val preferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val isFirstOpen = preferences.getBoolean("open_first", true)

        if (isFirstOpen) {
            showLogin()
        } else {
            markAppAlreadyOpen(preferences)
            showSplash()
        }
    }

    private fun showAlertMinVersion() {
        AlertDialog.Builder(this)
            .setTitle("Ops")
            .setMessage("Você esta utilizando uma versão muito antiga do aplicativo. Deseja atualizar?")

            .setPositiveButton(android.R.string.yes) { dialog, which ->
                var intent: Intent

                try {
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                    startActivity(intent)
                }
            }

            .setNegativeButton("Não") { dialog, which ->
                finish()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun markAppAlreadyOpen(preferences: SharedPreferences) {
        val editor = preferences.edit()
        editor.putBoolean("open_first", false)
        editor.apply()
    }

    private fun showLogin() {
        val nextScreen = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(nextScreen)
        finish()
    }

    private fun showSplash() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.animacao_splash)
        anim.reset()
        ivLogo.clearAnimation()
        ivLogo.startAnimation(anim)

        Handler().postDelayed({
            val nextScreen = Intent(this@SplashActivity, FormActivity::class.java)
            startActivity(nextScreen)
            finish()
        }, TEMPO_AGUARDO_SPLASHSCREEN)
    }
}
