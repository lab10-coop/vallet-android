package io.lab10.vallet

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.client.activity_client_home_activty.*
import kotlinx.android.synthetic.client.app_bar_client_home_activty.*
import io.lab10.vallet.models.Tokens
import kotlinx.android.synthetic.client.voucher_item.view.*


class ClientHomeActivty : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home_activty)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        val burger = toggle.drawerArrowDrawable
        burger.color = resources.getColor(R.color.black)
        toggle.syncState()

        Tokens.refresh()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        val menu = navigationView.menu
        Tokens.getVouchers().forEach { token ->
            val item = menu.add(token.name)
            item.isCheckable = true
            item.setActionView(R.layout.token_balance )
            val view = item.actionView
            view.voucherBalance.text = token.balance.toString()
            item.setOnMenuItemClickListener { _ ->
                val intent = Intent(this, ProductListActivity::class.java)
                val tokenAddress = token.tokenAddress
                intent.action = "Start"
                intent.putExtra("EXTRA_TOKEN_ADDRESS", tokenAddress)
                intent.putExtra("EXTRA_TOKEN_BALANCE", token.balance)
                intent.putExtra("EXTRA_TOKEN_TYPE", token.tokenType)
                ValletApp.activeToken = token
                startActivity(intent)
                true
            }
        }
        menu.addSubMenu("Settings")
        menu.add(resources.getString(R.string.tap_to_search_for_admin))
        val qrMenu = menu.add(resources.getString(R.string.your_qrcode))
        qrMenu.setOnMenuItemClickListener { _ ->
            val intent = Intent(this, ShowQrCodeActivity::class.java)
            startActivity(intent)
            true
        }
        navigationView.invalidate()
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
