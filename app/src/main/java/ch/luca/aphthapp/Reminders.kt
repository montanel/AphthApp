package ch.luca.aphthapp

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * The evening reminder and the next-morning nudge (DECISIONS D-014). Each is one alarm that,
 * when it goes off, shows its notification (the nudge only if yesterday wasn't logged and the
 * nudge is on) and sets itself again for the next day. Times follow the phone's local clock, so
 * they move with time-zone changes (D-025).
 */
object Reminders {
    private const val CHANNEL = "reminders"
    const val ACTION_EVENING = "ch.luca.aphthapp.EVENING"
    const val ACTION_MORNING = "ch.luca.aphthapp.MORNING"
    private const val MORNING_MINUTES = 9 * 60
    const val ID_EVENING = 1
    const val ID_MORNING = 2

    /** Intent extra for MainActivity: the log day to open as a past day (ISO date). */
    const val EXTRA_DAY = "day"

    /** Intent extra for MainActivity: open Today. */
    const val EXTRA_TODAY = "today"

    /** Sets both alarms from the current settings, replacing any already set. */
    fun schedule(context: Context) {
        Store.load(context)
        set(context, ACTION_EVENING, Store.eveningMinutes)
        set(context, ACTION_MORNING, MORNING_MINUTES)
    }

    /** The next moment, after now, when the local clock reads `minutes` after midnight. */
    private fun nextAt(minutes: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var at = now.toLocalDate().atTime(LocalTime.of(minutes / 60, minutes % 60))
        if (!at.isAfter(now)) at = at.plusDays(1)
        return at.atZone(zone).toInstant().toEpochMilli()
    }

    private fun set(context: Context, action: String, minutes: Int) {
        val alarms = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(
            context, action.hashCode(),
            Intent(context, ReminderReceiver::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val at = nextAt(minutes)
        if (Build.VERSION.SDK_INT < 31 || alarms.canScheduleExactAlarms()) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
    }

    /** Shows a notification that opens the app; with `day`, it opens that day as a past day. */
    fun show(context: Context, id: Int, title: String, text: String, day: LocalDate?) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (day != null) open.putExtra(EXTRA_DAY, day.toString()) else open.putExtra(EXTRA_TODAY, true)
        val tap = PendingIntent.getActivity(
            context, id, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = android.app.Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_aphthapp)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

}

/** Receives the two alarms, and the events after which they must be set again. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Store.load(context)
        when (intent.action) {
            Reminders.ACTION_EVENING -> Reminders.show(
                context, Reminders.ID_EVENING, "How was today?", "Takes 20 seconds. Tap to log.", null,
            )
            Reminders.ACTION_MORNING -> {
                val yesterday = logDate(LocalDateTime.now()).minusDays(1)
                if (Store.nudge && Store.days[yesterday] == null) Reminders.show(
                    context, Reminders.ID_MORNING, "Yesterday is still empty", "Fill it in while you remember.", yesterday,
                )
            }
        }
        // Covers the alarms themselves (next day), boot, app updates, and clock or zone changes.
        Reminders.schedule(context)
    }
}
