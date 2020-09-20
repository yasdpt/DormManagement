package ir.staryas.dormmanagement.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.plusAssign
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import ir.staryas.dormmanagement.navigation.KeepStateNavigator
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.databinding.ActivityAdminBinding
import ir.staryas.dormmanagement.fragment.StudentFragment
import ir.staryas.dormmanagement.util.*
import kotlinx.android.synthetic.main.activity_admin.*

class AdminActivity : AppCompatActivity(), StudentFragment.OnFragmentStudentInteractionListener {


    override fun onFragmentStudentInteraction(uri: Uri) {

    }

    companion object {
        val tools = Tools()
    }





    private lateinit var prefManage: PrefManage
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityAdminBinding
    private lateinit var toolbar: Toolbar
    private lateinit var navController: NavController
    private var goBack: Boolean = false

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin)
        prefManage = PrefManage(applicationContext)



        initToolbar()

        setupNavigation()

    }

    private fun setupNavigation() {
        navController = findNavController(R.id.nav_host_fragment)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.title.observe(this, Observer {
            supportActionBar?.title = it
        })

        viewModel.updateIsAtStudentState(false)



        // change menu if admin is supervisor
        val menu: Menu = nav_view.menu

        if (prefManage.getUserRole() == "supervisor"){
            menu.removeItem(R.id.navigation_dashboard)
        }

        // get fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!


        // setup custom navigator
        val navigator = KeepStateNavigator(
            this,
            navHostFragment.childFragmentManager,
            R.id.nav_host_fragment
        )
        navController.navigatorProvider += navigator




        // set navigation graph
        navController.setGraph(R.navigation.mobile_navigation)
        setupActionBarWithNavController(navController)
        val config: AppBarConfiguration = AppBarConfiguration.Builder(R.id.nav_host_fragment, R.id.navigation_home).build()



        binding.navView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_home -> {
                    viewModel.isStudent.observe(this, Observer { isStudent ->
                        if (isStudent){
                            viewModel.name.observe(this, Observer {
                                viewModel.updateActionBarTitle(it)
                            })
                            navController.navigate(R.id.navigation_student)
                        } else {
                            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                            navController.navigate(R.id.navigation_home)
                        }
                    })
                }
                R.id.navigation_dashboard -> {
                    viewModel.updateActionBarTitle("سرپرست ها")
                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                    navController.navigate(R.id.navigation_dashboard)
                }
                R.id.navigation_notifications -> {
                    viewModel.updateActionBarTitle("بیشتر")
                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                    navController.navigate(R.id.navigation_notifications)
                }
            }
            true
        }




        viewModel.isStudent.observe(this, Observer {
            if (it){
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            }
        })

    }

    override fun supportNavigateUpTo(upIntent: Intent) {
        navController.navigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initToolbar() {
        toolbar = findViewById(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.elevation = 0.0f
        tools.setSystemBarColor(this,
            R.color.colorPrimaryDark
        )
        tools.setSystemBarLight(this)
    }


    override fun onBackPressed() {
        viewModel.isAtStudent.observe(this, Observer {
            goBack = it
        })

        if (goBack){
            try{
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                navController.navigate(R.id.navigation_home)
            } catch (e:Exception){
                toast(e.message!!, this)
                Log.d("navProblem", e.message!!)
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                navController.navigate(R.id.navigation_home)
                return true
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }



}
