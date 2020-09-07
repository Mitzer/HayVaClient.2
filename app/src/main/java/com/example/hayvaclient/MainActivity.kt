package com.example.hayvaclient

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener
    private lateinit var dialog: android.app.AlertDialog
    private val compositeDisposable = CompositeDisposable()
    private val cloudsFunctions:ICloudFunctions

    companion object{
        private val APP_REQUEST_CODE = 7171
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if(listener != null)
            firebaseAuth.removeAuthStateListener(listener)
            compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        init()
    }

    private fun init(){
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cloudsFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if(user != null){

                // Logueado
                Toast.makeText(this,"Logueado",Toast.LENGTH_SHORT).show()
            }else{

                //No logueado
                val accessToken = AccountKit.getCurrentAccessToken()
                if(accessToken != null)
                    getCustomToken(accessToken)
                else
                    phoneLogin()

            }
        }
    }

    private fun phoneLogin(){
        val intent = Intent(this@MainActivity,AccountKitActivity::class.java)
        val configurationBuildConfig = AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN)
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_CONFIGURATION,configurationBuildConfig.build())
        startActivityForResult(intent, APP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == APP_REQUEST_CODE){
            handleFacebookLoginResult(resultCode,data)
        }
    }

    private fun handleFacebookLoginResult(resultCode: Int, data: Intent?){
        val result = data!!.getParcelableExtra>AccountKitLoginResult<(AccountKitLoginResult.RESULT_KEY)
        if(result!!.error != null){
            Toast.makeText(this,"" + result!!.error!!.userFacingMessage,Toast.LENGTH_SHORT).show()
        } else if(result.wasCancelled() || resultCode == Activity.RESULT_CANCELED){
            finish()
        } else{
            if (result.accessToken != null){
                getCustomToken(result.accessToken!!)
                Toast.makeText(this,"Login correcto",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCustomToken(accessToken:AccessToken){
        dialog.show()
        compositeDisposable.add(cloudsFunctions!!.getCustomToken(accessToken.token)
                .suscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ responseBody ->

                    val customToken = responseBody.string()
                    signInWithCustomToken(customToken)

                }, {t:Throwable? ->

                    dialog!!.dismiss()
                    Toast.makeText(this@MainActivity,""+t!!.message,Toast.LENGTH_SHORT).show()

                }) )
    }

    private fun signInWithCustomToken(customToken:String){
        dialog!!.dismiss()
        firebaseAuth!!.signInWithCustomToken(customToken)
                .addOnCompleteListener{ task ->
                    if(!task.isSuccessful){
                        Toast.makeText(this,"Autenticación fallida",Toast.LENGTH_SHORT).show()
                    }
                }
    }
}