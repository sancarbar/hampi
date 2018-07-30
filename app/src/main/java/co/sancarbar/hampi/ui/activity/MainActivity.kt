package co.sancarbar.hampi.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.TextView
import co.sancarbar.hampi.R
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import model.Plant
import ui.adapter.PlantsEntriesAdapter

const val NEW_ENTRY_REQUEST_CODE = 99

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FirebaseAuth.AuthStateListener {


    private var firebaseAuth = FirebaseAuth.getInstance()

    private val plantsAdapter = PlantsEntriesAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            startActivityForResult(Intent(this, NewEntryActivity::class.java), NEW_ENTRY_REQUEST_CODE)
        }

        configureNavigationDrawer()
        firebaseAuth.addAuthStateListener(this)
        configureRecyclerView()
    }

    private fun configureRecyclerView() {

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = plantsAdapter
    }

    private fun configureNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_share -> {

            }

            R.id.nav_logout -> {
                firebaseAuth.signOut()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val headerView = nav_view.getHeaderView(0)
            val username = headerView.findViewById<TextView>(R.id.username)
            username.text = user.displayName
            val email = headerView.findViewById<TextView>(R.id.email)
            email.text = user.email
            if (user.photoUrl != null) {
                val profileImage = headerView.findViewById<CircleImageView>(R.id.profile_image)
                Picasso.get().load(user.photoUrl).into(profileImage)
            }

        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == NEW_ENTRY_REQUEST_CODE) {
            val plant: Plant = data!!.getSerializableExtra(CREATED_ENTRY_KEY) as Plant
            plantsAdapter.add(plant)
        }
    }
}
