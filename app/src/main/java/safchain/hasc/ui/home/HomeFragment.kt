package safchain.hasc.ui.home

import android.Manifest
import android.content.Context
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import safchain.hasc.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val errorPage =
        """
            <?xml version="1.0" encoding="UTF-8" ?>
            <html>
            <head>
              <style>
                html,
                body {
                  min-height: 100vh;
                  overflow: auto;
                  font-family: Arial, Helvetica, sans-serif;
                }

                body {
                  text-align: center;
                  background-image: linear-gradient(112.1deg, #3a445e 11.4%, #1e2a46 70.2%);
                  background-image: -moz-linear-gradient(112.1deg, #3a445e 11.4%, #1e2a46 70.2%);
                  background-image: -webkit-linear-gradient(112.1deg, #3a445e 11.4%, #1e2a46 70.2%);
                  padding-top: 30px;
                  color: #e5e5e5;
                  height: 100%;
                  padding-bottom: 20px;
                }
              </style>
            </head>
            <body>
              <h1>Not available</h1>
            </body>
            </html>
        """.trimIndent()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return root
        val endpoint = sharedPref.getString("endpoint", "")
        val page = sharedPref.getString("page", "")

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setCookie("${endpoint}/", "settingsButton=false")

        val webView: WebView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        val vibrator: Vibrator = getActivity()?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        webView.addJavascriptInterface(WebAppInterface(vibrator), "AndroidHaptic")
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d("HASC", "${message.message()} -- From line " +
                        "${message.lineNumber()} of ${message.sourceId()}")
                return true
            }
        }
        webView.setWebViewClient(object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request!!.url.toString()

                if (url.startsWith("http")) {
                    // Return false means, web view will handle the link
                    return false
                }

                return false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.url.host == "192.168.123.1") {
                    if (request.url.scheme == "https") {
                        // try with http
                        val httpPage = page?.replace("https", "http", true)
                        if (httpPage != null) {
                            webView.loadUrl(httpPage)
                        }
                    }
                    else if (request.url.path == "/setup.html") {
                        webView.loadDataWithBaseURL(null, errorPage, "text/html", "UTF-8", null)
                    }
                } else {
                    webView.loadDataWithBaseURL(null, errorPage, "text/html", "UTF-8", null)
                }
            }
        })
        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        })

        val cameraPermission = registerForActivityResult<String, Boolean>(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.i("DEBUG", "permission granted")
            } else {
                Log.i("DEBUG", "permission denied")
            }
        }
        cameraPermission.launch(Manifest.permission.CAMERA)

        if (page != null && page.startsWith("http")) {
            webView.loadUrl(page)
        } else {
            webView.loadUrl("${endpoint}/app${page}")
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class WebAppInterface(vibrator: Vibrator) {
        private var vibrator: Vibrator? = null;

        init {
            this.vibrator = vibrator
        }

        @JavascriptInterface
        fun vibrate(duration: Long) {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator?.vibrate(100)
            }
        }
    }
}