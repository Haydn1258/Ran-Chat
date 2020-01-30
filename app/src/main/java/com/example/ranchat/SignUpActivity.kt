package com.example.ranchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.ranchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        signup_btnCheck.setOnClickListener {
            if(isValidEmail(signup_edtEmail.text.toString()) && isValidPasswd(signup_edtPassword.text.toString())) {
                signup()
            }
        }
        signup_txtvGuide.setOnClickListener {
            finish()
        }
        signup_imgvBack.setOnClickListener {
            finish()
        }


    }
    fun signup(){
        val firebase = FirebaseDatabase.getInstance()
        auth?.createUserWithEmailAndPassword(signup_edtEmail.text.toString(), signup_edtPassword.text.toString())
            ?.addOnCompleteListener {
                    task -> //성공시 Firebase에 회원정보 저장
                if (task.isSuccessful){
                    val user = User()
                    user.userID = signup_edtEmail.text.toString()
                    user.userNickname = signup_edtNickname.text.toString()
                    user.uid = auth?.uid
                    firestore?.collection("user")?.document(user.uid!!)?.set(user)
                    //firestore?.collection("user")?.document(user.userID)?.collection("friend")?.add("user")
                    Toast.makeText(applicationContext, "회원가입완료", Toast.LENGTH_SHORT).show()
                    this@SignUpActivity.finish()
                    //실패시
                }else{
                    Toast.makeText(applicationContext, "중복되는 이메일입니다.", Toast.LENGTH_SHORT).show()
                    signup_edtEmail.setText("")
                    signup_edtPassword.setText("")
                    signup_edtNickname.setText("")
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
                signup_edtEmail.setText("")
                return false
            }
            else-> {return true }
        }
    }

    // 비밀번호 유효성 검사
    fun isValidPasswd(password:String): Boolean {
        when{
            password.isEmpty()-> {
                // 비밀번호 공백
                Toast.makeText(applicationContext, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return false
            }
            !PASSWORD_PATTERN.matcher(password).matches() -> {
                // 비밀번호 형식 불일치
                Toast.makeText(applicationContext, "비밀번호 형식이 맞지 않습니다.", Toast.LENGTH_SHORT).show()
                signup_edtPassword.setText("")
                return false
            }
            else -> { return true }
        }
    }
    companion object{
        val PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{6,12}$")
    }
}
