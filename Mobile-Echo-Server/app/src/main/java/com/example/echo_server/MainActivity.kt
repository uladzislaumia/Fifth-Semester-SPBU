package com.example.echo_server

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var disp: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        disp = Retranslator.logs
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                addLog(it)
            }
    }

    fun addLog(msg: String) {
        log.text = "${log.text}\n${msg}";
    }

    override fun onDestroy() {
        super.onDestroy()
        disp?.dispose()
    }
}
