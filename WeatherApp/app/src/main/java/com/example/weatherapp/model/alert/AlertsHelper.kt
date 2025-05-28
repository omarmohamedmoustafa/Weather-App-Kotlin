package com.example.weatherapp.model.alert

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.weatherapp.model.pojos.Alert

class AlertsHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setAlarm(alert: Alert) {
        val intent = Intent(context, AlertReceiver::class.java).apply {
            action = AlertReceiver.ACTION_FIRE_ALERT
            putExtra("ALERT_ID", alert.alertId)
            putExtra("TO_TIME", alert.alertStopAt)
            putExtra("IS_ALARM", alert.isAlarm)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alert.alertId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, alert.alertTriggerAt, pendingIntent
        )
    }

    fun cancelAlarm(alert: Alert) {
        // Cancel trigger intent
        val triggerIntent = Intent(context, AlertReceiver::class.java).apply {
            action = AlertReceiver.ACTION_FIRE_ALERT
        }
        val triggerPendingIntent = PendingIntent.getBroadcast(
            context, alert.alertId.toInt(), triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(triggerPendingIntent)

        // Cancel stop intent (for alarm alerts)
        val stopIntent = Intent(context, AlertReceiver::class.java).apply {
            action = AlertReceiver.ACTION_DISMISS_ALERT
            putExtra("ALERT_ID", alert.alertId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, alert.alertId.toInt() + 1000, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(stopPendingIntent)

        // Cancel dismiss intent
        val dismissIntent = Intent(context, AlertReceiver::class.java).apply {
            action = AlertReceiver.ACTION_DISMISS_ALERT
            putExtra("ALERT_ID", alert.alertId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, alert.alertId.toInt() + 2000, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(dismissPendingIntent)

        // Stop ringtone if playing
        AlertReceiver.currentRingtone?.let { ringtone ->
            if (ringtone.isPlaying) {
                ringtone.stop()
            }
            AlertReceiver.currentRingtone = null
        }
    }
}