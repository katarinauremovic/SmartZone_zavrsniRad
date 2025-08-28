package com.example.smartzone.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.smartzone.entities.PlannerEvent
import com.example.smartzone.receiver.PlannerReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.*

class PlannerHelper(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    fun observeEventsDirect(onChange: (List<PlannerEvent>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val ref = firestore.collection("users").document(uid).collection("planner")
        ref.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val events = snapshot?.documents?.mapNotNull {
                it.toObject(PlannerEvent::class.java)?.copy(id = it.id)
            } ?: emptyList()
            onChange(events)
        }
    }


    suspend fun saveEvent(event: PlannerEvent): String? {
        val uid = auth.currentUser?.uid ?: return null
        val ref = firestore.collection("users").document(uid).collection("planner")

        val docRef = if (event.id == null) {
            val newEventRef = ref.document()
            newEventRef.set(event.copy(id = null))
            newEventRef
        } else {
            ref.document(event.id).set(event.copy(id = null))
            ref.document(event.id)
        }


        val eventId = docRef.id
        scheduleReminder(event.copy(id = eventId))
        return eventId
    }

    suspend fun deleteEvent(eventId: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = firestore.collection("users").document(uid).collection("planner").document(eventId)
        ref.delete().await()
        cancelReminder(eventId)
    }

    fun scheduleReminder(event: PlannerEvent) {
        val triggerTime = calculateNextOccurrence(event) ?: return

        val reminderMillis = (event.reminderMinutesBefore ?: 10) * 60 * 1000
        val finalTriggerTime = triggerTime - reminderMillis

        val intent = Intent(context, PlannerReceiver::class.java).apply {
            putExtra("title", event.title)
            putExtra("eventId", event.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalTriggerTime, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, finalTriggerTime, pendingIntent)
        }
    }

    fun cancelReminder(eventId: String?) {
        if (eventId == null) return
        val intent = Intent(context, PlannerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)
    }

    fun calculateNextOccurrence(event: PlannerEvent): Long? {
        val zoneId = ZoneId.of(event.timezone)
        val now = ZonedDateTime.now(zoneId)

        val targetDay = DayOfWeek.of(event.weekday)
        var date = now.toLocalDate()

        val eventTime = LocalTime.of(event.startMinutes / 60, event.startMinutes % 60)
        val eventDateTimeToday = date.atTime(eventTime)

        if (now.dayOfWeek == targetDay && now.toLocalTime().isBefore(eventTime)) {
            date = now.toLocalDate()
        } else {
            do {
                date = date.plusDays(1)
            } while (date.dayOfWeek != targetDay)
        }

        val resultDateTime = date.atTime(eventTime)
        return resultDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }
    suspend fun addEventFromDialog(title: String, weekday: Int, hour: Int, minute: Int, reminder: Int) {
        val event = PlannerEvent(
            id = null,
            title = title,
            weekday = weekday,
            startMinutes = hour * 60 + minute,
            reminderMinutesBefore = reminder,
            timezone = ZoneId.systemDefault().id
        )
        saveEvent(event)
    }

}
