package io.github.kaisendonone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.kaisendonone.Fragment.TimeLineFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fragment
        supportFragmentManager.beginTransaction()
            .replace(main_activity_parent_linearlayout.id, TimeLineFragment()).commit()

        // ばーいらん
        supportActionBar?.hide()

    }
}
