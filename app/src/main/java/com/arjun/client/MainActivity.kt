package com.arjun.client

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val serviceIntent: Intent by lazy { Intent() }

    private var randomNumberRequestMessenger: Messenger? = null
    private var randomNumberReceiverMessenger: Messenger? = null

    private var isServiceBound: Boolean = false
    private var randomNumber: Int = -1

    private val serviceConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                randomNumberRequestMessenger = Messenger(service)
                randomNumberReceiverMessenger = Messenger(ReceiveRandomNumberHandler())
                isServiceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                randomNumberRequestMessenger = null
                randomNumberReceiverMessenger = null
                isServiceBound = false
            }

        }
    }

    inner class ReceiveRandomNumberHandler : Handler() {
        override fun handleMessage(msg: Message) {
            randomNumber = 0
            if (msg.what == GET_RANDOM_NUMBER_FLAG) {
                randomNumber = msg.arg1
                random_number.text = "$randomNumber"
            }
            super.handleMessage(msg)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceIntent.component = ComponentName("com.arjun.services", "com.arjun.services.MyService")

        bind_service.setOnClickListener {
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
            Toast.makeText(this, "Service Bonded", Toast.LENGTH_LONG).show()
        }

        unbind_service.setOnClickListener {
            unbindService(serviceConnection)
            isServiceBound = false
            Toast.makeText(this, "Service Unbonded", Toast.LENGTH_LONG).show()
        }

        get_random_number.setOnClickListener {
            if (isServiceBound) {
                val message = Message.obtain(null, GET_RANDOM_NUMBER_FLAG)
                message.replyTo = randomNumberReceiverMessenger

                try {
                    randomNumberRequestMessenger?.send(message)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            } else {
                Toast.makeText(this, "Service Unbound", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val GET_RANDOM_NUMBER_FLAG = 0
    }
}