package safchain.hasc.ui.home

import android.Manifest
import android.content.Context
import android.os.Vibrator
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
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

        webView.loadUrl("${endpoint}/app${page}")

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