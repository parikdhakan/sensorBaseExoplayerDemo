package com.demo.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.demo.app.databinding.ActivityMainBinding
import com.demo.app.media3.WatchActivity

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI()
        startActivity(Intent(this@MainActivity, WatchActivity::class.java))
    }

    private fun setupUI() {
        binding.apply {
            btnWatch.setOnClickListener {
                startActivity(Intent(this@MainActivity, WatchActivity::class.java))
            }
        }
    }
}