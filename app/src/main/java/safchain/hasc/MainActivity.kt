package safchain.hasc

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import safchain.hasc.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var settings: Boolean = false
    private var home: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_devices, R.id.nav_add, R.id.nav_setup_wifi, R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("page", "")
            apply()
        }

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("page", "")
            apply()
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(R.id.nav_home)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return true
        val charts = sharedPref.getString("charts", "")

        val id = item.itemId
        when (id) {
            R.id.nav_home -> {
                with(sharedPref.edit()) {
                    putString("page", "")
                    apply()
                }
            }
            R.id.nav_add -> {
                with(sharedPref.edit()) {
                    putString("page", "/add.html")
                    apply()
                }
                home = false
            }
            R.id.nav_devices -> {
                with(sharedPref.edit()) {
                    putString("page", "/list.html")
                    apply()
                }
                home = false
            }
            R.id.nav_setup_wifi -> {
                with(sharedPref.edit()) {
                    putString("page", "https://192.168.123.1/setup.html")
                    apply()
                }
                home = false
            }
            R.id.nav_charts -> {
                with(sharedPref.edit()) {
                    putString("page", charts)
                    apply()
                }
                home = false
            }
            R.id.nav_logout -> {
                with(sharedPref.edit()) {
                    putString("page", "/login.html")
                    apply()
                }
                home = false
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        drawerLayout.closeDrawers()

        navController.navigate(R.id.nav_home)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_settings)
                settings = true
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (settings) {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_home)
            settings = false
        } else {
            val webView: WebView = findViewById(R.id.webView)
            if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack()
                return true
            } else if (!home) {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return true
                with(sharedPref.edit()) {
                    putString("page", "")
                    apply()
                }
                navController.navigate(R.id.nav_home)
                home = true
            }
            return super.onKeyDown(keyCode, event)
        }
        return true
    }
}