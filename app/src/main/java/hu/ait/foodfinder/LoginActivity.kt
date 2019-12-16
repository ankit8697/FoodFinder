package hu.ait.foodfinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginClick(v: View) {
        if (!isFormValid()) {
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(etEmail.text.toString(),
            etPassword.text.toString()
        ).addOnSuccessListener {

            Toast.makeText(this@LoginActivity, getString(R.string.logged_in), Toast.LENGTH_LONG).show()
            // Open FormActivity
            startActivity(Intent(this@LoginActivity, ForumActivity::class.java))
        }.addOnFailureListener {
            Toast.makeText(this@LoginActivity, "Error: ${it.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    fun registerClick(v: View) {
        if (!isFormValid()) {
            return
        }


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnSuccessListener {
            val user = it.user
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(userNameFromEmail(etEmail.text.toString()))
                    .build()
            )
            Toast.makeText(this@LoginActivity, getString(R.string.registration_ok),
                Toast.LENGTH_LONG).show()

        }.addOnFailureListener {
            Toast.makeText(this@LoginActivity, "Error: ${it.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun userNameFromEmail(email: String) = email.substringBefore("@")

    private fun isFormValid() : Boolean {
        return when {
            etEmail.text.isEmpty() -> {
                etEmail.error = getString(R.string.empty_field_error)
                false
            }
            etPassword.text.isEmpty() -> {
                etPassword.error = getString(R.string.empty_field_error)
                false
            }
            else -> true
        }
    }


}
