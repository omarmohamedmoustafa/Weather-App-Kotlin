package com.example.weatherapp.model.pojos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true)
    val alertId: Long = 0,
    val dateMillis : Long,
    val alertTriggerAt: Long,
    val alertStopAt: Long,
    val isAlarm: Boolean
)