package be.sciensano.coronalert.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import be.sciensano.coronalert.util.DateUtil
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ActivityLinkTestBinding
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.DialogHelper

class LinkTestActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = LinkTestActivity::class.simpleName
        private const val URL_ARGUMENT = "url"
        private const val R1_ARGUMENT = "r1"
        private const val T0_ARGUMENT = "t0"
        const val ACTIVATION_RESULT = 100
        private val CONFIRMATION_URLS = listOf(
            "corona-alert-form-confirmation",
            "formulaire-coronalert-confirmation",
            "coronalert-formulier-bevestiging",
            "coronalert-formular-bestatigung"
        )

        fun start(activity: Activity, url: String, r1: String, t0: String) {
            val intent = Intent(activity, LinkTestActivity::class.java)
            intent.putExtra(URL_ARGUMENT, url)
            intent.putExtra(R1_ARGUMENT, r1)
            intent.putExtra(T0_ARGUMENT, t0)

            activity.startActivityForResult(intent, ACTIVATION_RESULT)
        }
    }

    private lateinit var binding: ActivityLinkTestBinding

    private lateinit var url: String
    private lateinit var testCode: String
    private lateinit var dateInfectious: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_link_test)

        val startingUrl = intent.extras?.getString(URL_ARGUMENT) ?: throw IllegalStateException()
        testCode = intent.extras?.getString(R1_ARGUMENT) ?: throw IllegalStateException()
        dateInfectious = intent.extras?.getString(T0_ARGUMENT) ?: throw IllegalStateException()


        binding.informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            finish()
        }

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true

        @Suppress("MagicNumber")
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress != 0 && newProgress != 100) {
                    binding.progress.visibility = View.VISIBLE
                    view.visibility = View.INVISIBLE
                } else {
                    binding.progress.visibility = View.INVISIBLE
                    view.visibility = View.VISIBLE
                }
            }
        }

        binding.webView.webViewClient = object : CustomBrowser() {}

        url = "${startingUrl}&read-only=1"

        dateInfectious = android.text.format.DateFormat.format(
            "dd/MM/yyyy",
            DateUtil.parseServerDate(dateInfectious).toDate()
        ) as String

        if (ConnectivityHelper.isNetworkEnabled(this)) {
            binding.webView.loadUrl(url)
        } else {
            DialogHelper.showDialog(
                DialogHelper.DialogInstance(
                    this@LinkTestActivity,
                    R.string.activity_link_test_error_title,
                    R.string.activity_link_test_error_body,
                    R.string.activity_link_test_error_button_positive,
                    R.string.activity_link_test_error_button_negative,
                    false,
                    { binding.webView.loadUrl(url) },
                    { finish() }
                )
            )
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    open inner class CustomBrowser : WebViewClient() {
        override fun onPageFinished(view: WebView, newUrl: String) {
            if (newUrl == url) {
                //strip html
                view.loadUrl(
                    "javascript:" +
                            "if(document.getElementById('cookie-notice')) " +
                            "document.getElementById('cookie-notice').style.display = 'none';" +
                            "document.getElementsByClassName('navbar')[0].style.display = 'none';" +
                            "document.getElementsByClassName('entry-header')[0].style.display = 'none';" +
                            "document.getElementsByClassName('vc_row')[0].style.display = 'none';" +
                            "document.getElementsByClassName('vc_row')[1].style.display = 'none';" +
                            "document.getElementsByClassName('upper-footer')[0].style.display = 'none';" +
                            "document.getElementById('wrapper').style.paddingTop = '0';null"

                )
                //set inputs
                view.loadUrl(
                    "javascript:" +
                            "setCode('${testCode}');" +
                            "setDate('${dateInfectious}');"
                )
            }
        }

        @Suppress("ReturnCount")
        override fun shouldOverrideUrlLoading(
            view: WebView,
            newUrl: String
        ): Boolean {
            if (CONFIRMATION_URLS.any { confirmationUrl ->
                    Uri.parse(newUrl).toString().contains(confirmationUrl)
                }) {
                setResult(Activity.RESULT_OK)
                finish()
                return true
            }

            if (Uri.parse(newUrl).host == Uri.parse(url).host) {
                // This is my web site, so do not override; let my WebView load the page
                return false
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl))
            startActivity(intent)
            return true
        }
    }
}
