package com.example.echo_server

import android.app.Application

class App : Application() {

    lateinit var server: Server

    override fun onCreate() {
        super.onCreate()
        server = Server(5555)
        Thread{server.start()}.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        server.close()
    }
}