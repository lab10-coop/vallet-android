package io.lab10.vallet

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.client.activity_create_user.*

class CreateUserActivity : AppCompatActivity() {

    companion object {
        const val NAME_EXTRA = "USERNAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        getStarterd.setOnClickListener {
            if (inputUserName.text.length > 3 ) {
                val intent = Intent(this, ClientHomeActivity::class.java)
                intent.putExtra(NAME_EXTRA, inputUserName.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, resources.getString(R.string.create_user_error), Toast.LENGTH_LONG).show()
            }
        }
    }
}
