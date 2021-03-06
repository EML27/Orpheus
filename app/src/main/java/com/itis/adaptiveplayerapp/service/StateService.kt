package com.itis.adaptiveplayerapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.itis.adaptiveplayerapp.R
import com.itis.adaptiveplayerapp.bl.StateReturner
import com.itis.adaptiveplayerapp.bl.dto.StateDto
import com.itis.adaptiveplayerapp.di.component.DaggerStateReturnerComponent
import com.itis.adaptiveplayerapp.ui.view.StarterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class StateService : Service() {

    init {
        DaggerStateReturnerComponent.create().inject(this)
    }

    @Inject
    lateinit var stateReturner: StateReturner

    private var learning = false
    private var playing = false
    val CHANNEL_ID = "27"
    private var listeningThread: ListeningThread? = null

    override fun onBind(intent: Intent): IBinder = mBinder

    inner class MyBinder : Binder() {
        fun getService() = this@StateService
    }

    private val mBinder = MyBinder()

    fun start() {
        playing = true

        if (listeningThread == null) {
            listeningThread =
                ListeningThread().also { it.start() }
        }
    }

    fun learn() {
        learning = true
        if (listeningThread == null) {
            listeningThread =
                ListeningThread().also { it.start() }
        }
    }

    fun stopLearning() {
        learning = false
        if (!playing) {
            stop()
        }
    }

    fun stop() {
        learning = false
        playing = false
        listeningThread?.mustGo = false
        listeningThread = null
        SpotifyPlayerService.pauseMusic(this)
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent.let { intent ->
            when (intent?.action) {
                "start" -> {
                    start()
                }
                "learn" -> {
                    learn()
                }

                "stop_learn" -> {
                    stopLearning()
                }

                "stop" -> {
                    stop()
                }
                else -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun setNotification(state: StateDto) {

        val currentStateTitle: String

        //Логика установки текущего состояния
        currentStateTitle = state.occupation + " " + state.time + " " + state.weather
        // оаоаоаоаоаоаоа
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                notificationManager.getNotificationChannel(CHANNEL_ID) ?: NotificationChannel(
                    CHANNEL_ID,
                    name,
                    importance
                ).apply {
                    description = descriptionText
                }
            notificationManager.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(this, StarterActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentStateTitle)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1, notification)

        startForeground(1, notification)
        if (learning) {
            SpotifyPlayerService.startLearning(this, state)
        }
        if (playing) {
            SpotifyPlayerService.startMusic(this, state)
        }


    }

    inner class ListeningThread : Thread() {

        var mustGo = true
        private val waitTime = 10000.toLong()

        override fun run() {
            super.run()
            while (mustGo) {
                CoroutineScope(Dispatchers.IO).launch {
                    val state = stateReturner.getState()
                    setNotification(state)
                }
                sleep(waitTime)
            }
        }
    }

}
