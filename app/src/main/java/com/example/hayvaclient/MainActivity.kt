package com.example.hayvaclient

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.example.hayvaclient.Common.Common
import com.example.hayvaclient.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_register.*

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener
    private lateinit var dialog: android.app.AlertDialog
    private val compositeDisposable = CompositeDisposable()
    private val cloudsFunctions:ICloudFunctions

    private lateinit var userRef:DatabaseReference

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
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cloudsFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if(user != null){

                // Logueado
                //Toast.makeText(this,"Logueado",Toast.LENGTH_SHORT).show()
                AccountKit.getCurrentAccount(object:AccountKitCallback<Account>{
                    override fun onSuccess(account:Account?){
                        checkUserFromFirebase(user!!.uid, account!!)
                    }

                    override fun onError(p0:AccounKitError?){
                        Toast.makeText(this@MainActivity,""+p0.errorType.message,Toast.LENGTH_SHORT).show()
                    }
                })

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

    private fun checkUserFromFirebase(uid: String, account: Account) {
        dialog!!.show()
        userRef!!.child(uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity,""+error.message,Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){

                        val userModel = snapshot.getValue(UserModel::class.java)
                        goToHomeActivity(userModel)

                    }else{

                        showRegisterDialog(uid, account)

                    }

                    dialog!!.dismiss()

                }

            })
    }

    private fun showRegisterDialog(uid: String, account: Account) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.layout_register,null)

        val edit_name = itemView.findViewById<EditText>(R.id.edt_name)
        val edit_address = itemView.findViewById<EditText>(R.id.edt_address)
        val edit_phone = itemView.findViewById<EditText>(R.id.edt_phone)

        // Set
        edit_phone.setText(account.phoneNumber.toString())

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") {dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton("REGISTER") {dialogInterface, i ->
            if(TextUtils.isDigitsOnly(edit_name.text.toString())){
                Toast.makeText(this@MainActivity,"Por favor ingresa tu nombre",Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }else if(TextUtils.isDigitsOnly(edit_address.text.toString())){
                Toast.makeText(this@MainActivity,"Por favor ingresa tu dirección",Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val userModel = UserModel()
            userModel.uid = uid
            userModel.name = edt_name.text.toString()
            userModel.address = edt_address.text.toString()
            userModel.phone = edt_phone.text.toString()

            userRef!!.child(uid)
                .setValue(userModel)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){

                        dialogInterface.dismiss()
                        Toast.makeText(this@MainActivity,"Registro completo",Toast.LENGTH_SHORT).show()

                        goToHomeActivity(userModel)
                    }
                }
        }


        // IMPORTANTE - MOSTRAR EL DIALOGO
        val dialog = builder.create()
        dialog.show()
    }

    private fun goToHomeActivity(userModel: UserModel?) {
        Common.currentUser = userModel!!
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