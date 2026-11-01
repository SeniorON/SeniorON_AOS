package com.example.senior_on.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.local.ParentConnectionGate
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.example.senior_on.data.source.device.AndroidDeviceStatusDataSource

internal const val BATTERY_CHANGED = 1
internal const val CHARGING_CHANGED = 2
internal const val NETWORK_RECOVERED = 4

/** Only field names are buffered, never historical battery/network values. */
internal class DeviceStatusChanges {
    private var battery: Int? = null
    private var charging: Boolean? = null
    private var online = false

    fun observe(level: Int, isCharging: Boolean, isOnline: Boolean): Int {
        var fields = 0
        if (isOnline) {
            if (!online) fields = NETWORK_RECOVERED
            if (battery != null && battery != level) fields = fields or BATTERY_CHANGED
            if (charging != null && charging != isCharging) fields = fields or CHARGING_CHANGED
        }
        battery = level
        charging = isCharging
        online = isOnline
        return fields
    }
}

/** Process-lifetime callbacks. Periodic WorkManager sync remains the process-death fallback. */
object DeviceStatusChangeObserver {
    private var registered = false

    @Synchronized fun register(context: Context) {
        if (registered) return
        registered = true
        val app = context.applicationContext
        val handler = Handler(Looper.getMainLooper())
        val source = AndroidDeviceStatusDataSource(app)
        val changes = DeviceStatusChanges()
        var fields = 0
        var account: String? = null
        val flush = Runnable {
            val pendingFields = fields
            val pendingAccount = account
            fields = 0
            account = null
            if (pendingFields != 0 && pendingAccount != null && source.getStatusSnapshot().networkConnected) {
                DeviceStatusPatchScheduler.enqueue(app, pendingAccount, pendingFields)
            }
        }
        fun observe() {
            handler.post {
                val snapshot = source.getStatusSnapshot()
                val changed = changes.observe(snapshot.batteryLevel, snapshot.charging, snapshot.networkConnected)
                val session = PersistedSessionStore(app).getSession()
                if (!snapshot.networkConnected || session?.role != UserRole.PARENT ||
                    AccessTokenStore.getBearerToken() == null || !ParentConnectionGate.isReady()) {
                    handler.removeCallbacks(flush)
                    fields = 0
                    account = null
                } else if (changed != 0) {
                    if (account != session.userId) fields = 0
                    account = session.userId
                    val first = fields == 0
                    fields = fields or changed
                    // Fixed five-second window: continuous events cannot postpone indefinitely.
                    if (first) handler.postDelayed(flush, 5_000L)
                }
            }
        }
        app.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = observe()
        }, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        app.getSystemService(ConnectivityManager::class.java)?.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = observe()
                override fun onLost(network: Network) = observe()
            },
        )
    }
}
