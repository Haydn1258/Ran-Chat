package com.example.ranchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.ranchat.SignUpActivity.Companion.PASSWORD_PATTERN
import com.example.ranchat.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
     
        login_txtvGuide.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        login_btnCheck.setOnClickListener {
            if(isValidEmail(login_edtEmail.text.toString()) && isValidPasswd(login_edtPassword.text.toString())) {
                signIn()
            }
        }
    }
    fun signIn(){
        auth?.signInWithEmailAndPassword(login_edtEmail.text.toString(), login_edtPassword.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if (task.isSuccessful){
                    Toast.makeText(applicationContext, "로그인성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    var firestore : FirebaseFirestore? = FirebaseFirestore.getInstance()


                    firestore?.collection("user")?.document(FirebaseAuth.getInstance().currentUser?.uid!!)?.get()?.
                        addOnCompleteListener{
                            if(it==null){
                                Log.d("snapshot","null")
                            }else{
                                var user = it.result?.toObject(User::class.java)
                                user?.timeStamp = System.currentTimeMillis()
                                firestore.collection("currentUser").document(FirebaseAuth.getInstance().currentUser?.uid!!).get().
                                    addOnCompleteListener{
                                        var currentUser = it.result?.toObject(User::class.java)
                                        if(it==null){
                                            Log.d("snapshot","null")
                                        }else{
                                            if (currentUser?.uid!=null){
                                                firestore.collection("currentUser").document(user?.uid!!).delete()
                                                firestore.collection("currentUser").document(user?.uid!!).set(user)

                                            }else{
                                                firestore.collection("currentUser").document(user?.uid!!).set(user)
                                            }

                                        }
                                    }
                            }
                        }



                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(applicationContext, "로그인실패",Toast.LENGTH_SHORT).show()
                    login_edtEmail.setText("")
                    login_edtPassword.setText("")
                }
            }
    }
    fun isValidEmail(email:String): Boolean {
        when{
            email.isEmpty() -> {
                // 이메일 공백
                Toast.makeText(applicationContext, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                // 이메일 형식 불일치
                Toast.makeText(applicationContext, "이메일 형식이 맞지 않습니다.", Toast.LENGTH_SHORT).show()
                login_edtEmail.setText("")
                return false
            }
            else-> {return true }
        }
    }

    // 비밀번호 유효성 검사
    fun isValidPasswd(password:String): Boolean {
        when {
            password.isEmpty() -> {
                // 비밀번호 공백
                Toast.makeText(applicationContext, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> {
                return true
            }
        }
    }


}
