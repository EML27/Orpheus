package com.itis.adaptiveplayerapp.service.interactor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.itis.adaptiveplayerapp.App
import com.itis.adaptiveplayerapp.service.StateService
import javax.inject.Inject

class StateServiceInteractor @Inject constructor() {


    var stateService: StateService? = null
    private var mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            stateService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StateService.MyBinder
            stateService = binder.getService()
        }
    }

    fun bind() =
        App.getInstance()?.bindService(
            Intent(App.getInstance(), StateService::class.java),
            mConnection,
            Context.BIND_AUTO_CREATE
        )


    fun startStateService() {
        App.getInstance()?.startService(Intent(App.getInstance(), StateService::class.java).apply {
            action = "start"
        })
    }

    fun stopStateService(){
        App.getInstance()?.startService(Intent(App.getInstance(),StateService::class.java).apply { action="stop" })
    }
}