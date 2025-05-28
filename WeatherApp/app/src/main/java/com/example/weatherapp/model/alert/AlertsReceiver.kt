package com.example.weatherapp.model.alert

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.weatherapp.activities.MyApplication
import com.example.weatherapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_FIRE_ALERT = "FIRE_ALERT" // Action to fire the alert at the FromTime
        const val ACTION_DISMISS_ALERT = "DISMISS_ALERT" // Action to dismiss the alert at the ToTime
        const val ACTION_ALARM_DISMISSED = "ALARM_DISMISSED" // Action to broadcast when an alarm is dismissed to update the UI
        internal var currentRingtone: Ringtone? = null
    }
    override fun onReceive(context: Context, intent: Intent) {
        var action = intent.action
        val alarmId = intent.getLongExtra("ALERT_ID", -1)
        val toTime = intent.getLongExtra("TO_TIME", 0)
        val alarmEnabled = intent.getBooleanExtra("IS_ALARM", false)
        when {
            action == ACTION_FIRE_ALERT -> {
                val hasPermission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    return
                }

                val dismissIntent = Intent(context, AlertReceiver::class.java).apply {
                    action = ACTION_DISMISS_ALERT
                    putExtra("ALERT_ID", alarmId)
                }

                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context, alarmId.toInt() + 2000,
                    dismissIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, "alerts_channel")
                    .setSmallIcon(R.drawable.c_alert)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setDeleteIntent(dismissPendingIntent)

                if (alarmEnabled) {
                    // Alarm alert: Play ringtone and add dismiss action
                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val ringtone = RingtoneManager.getRingtone(context, alarmSound)
                    currentRingtone?.let { if (it.isPlaying) it.stop() }
                    currentRingtone = ringtone
                    ringtone.play()

                    builder.setContentTitle("WeatherApp Alarm with id $alarmId")
                        .addAction(R.drawable.ic_delete, "Dismiss", dismissPendingIntent)

                    // Schedule stop alarm
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    val stopIntent = Intent(context, AlertReceiver::class.java).apply {
                        action = ACTION_DISMISS_ALERT
                        putExtra("ALERT_ID", alarmId)
                    }
                    val stopPendingIntent = PendingIntent.getBroadcast(
                        context, alarmId.toInt() + 1000, // Unique request code for stop
                        stopIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, toTime, stopPendingIntent
                    )
                } else {
                    builder.setContentTitle("WeatherApp Notification with id $alarmId")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                }

                NotificationManagerCompat.from(context).notify(alarmId.toInt(), builder.build())
            }

            action == ACTION_DISMISS_ALERT || (action == null && alarmId != -1L) -> {

                currentRingtone?.let { if (it.isPlaying) it.stop() }
                currentRingtone = null

                NotificationManagerCompat.from(context).cancel(alarmId.toInt())

                val application = context.applicationContext as MyApplication
                val repository = application.weatherRepository
                val alertsHelper = AlertsHelper(context)
                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = repository.getAlertById(alarmId)
                    repository.deleteAlert(alarm!!.alertId)
                    alertsHelper.cancelAlarm(alarm)
                    // Broadcast dismissal to update UI
                    val broadcastIntent = Intent(ACTION_ALARM_DISMISSED)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
                }
            }
        }
    }
}