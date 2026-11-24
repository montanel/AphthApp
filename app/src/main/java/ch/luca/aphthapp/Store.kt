package ch.luca.aphthapp

import android.content.Context
import android.util.AtomicFile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Executors

/**
 * The whole log and the settings, kept in memory as Compose state and saved to one JSON file
 * (`files/log.json`, included in Android Auto Backup) after every change.
 *
 * File layout:
 * ```
 * {"version": 1,
 *  "settings": {"sortRecent": false, "eveningMinutes": 1290, "nudge": true},
 *  "items": [{"key": "tea", "label": "Tea", "group": "Drinks", "type": "count",
 *             "visible": true, "order": 0, "testable": null, "lastTapped": 1764010000000}, ...],
 *  "days": {"2026-11-24": {"aphtha": false, "values": {"tea": 2, "coffee": 0, ...},
 *                          "zone": "America/New_York"}, ...}}
 * ```
 * Call [load] once before using it.
 */
/** Default evening reminder time: 21:30, in minutes after midnight. */
const val DEFAULT_EVENING = 21 * 60 + 30

object Store {
    private lateinit var file: AtomicFile
    private val writer = Executors.newSingleThreadExecutor()

    /** Every logged day. */
    val days = mutableStateMapOf<LocalDate, DayRecord>()

    /** All items, shown and hidden, including ones Luca added. */
    var items by mutableStateOf(DEFAULT_ITEMS)
        private set

    /** True when Today shows the "most recent" order instead of categories. */
    var sortRecent by mutableStateOf(false)
        private set

    /** Evening reminder time, in minutes after midnight (default 21:30). */
    var eveningMinutes by mutableIntStateOf(DEFAULT_EVENING)
        private set

    /** Whether the 09:00 nudge comes when the day before wasn't logged. */
    var nudge by mutableStateOf(true)
        private set

    /** Counts the times the app came to the front; Today works out its "most recent" order again each time. */
    var resumes by mutableIntStateOf(0)
        private set

    /** The day being logged now (see [logDate]); refreshed by [refreshToday]. */
    var today by mutableStateOf(logDate(LocalDateTime.now()))
        private set

    fun load(context: Context) {
        if (::file.isInitialized) return
        file = AtomicFile(File(context.filesDir, "log.json"))
        val text = try {
            file.readFully().decodeToString()
        } catch (_: java.io.FileNotFoundException) {
            return
        }
        val root = JSONObject(text)
        // "sortOften" is the name used before "most recent" replaced "often used".
        root.optJSONObject("settings")?.let {
            sortRecent = it.optBoolean("sortRecent", it.optBoolean("sortOften", false))
            eveningMinutes = it.optInt("eveningMinutes", DEFAULT_EVENING)
            nudge = it.optBoolean("nudge", true)
        }
        root.optJSONArray("items")?.let { arr ->
            val stored = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Item(
                    key = o.getString("key"),
                    label = o.getString("label"),
                    group = o.getString("group"),
                    type = ItemType.of(o.getString("type")),
                    visible = o.optBoolean("visible", true),
                    order = o.optInt("order", 0),
                    testable = Testable.of(if (o.isNull("testable")) null else o.getString("testable")),
                    lastTapped = if (o.isNull("lastTapped")) null else o.optLong("lastTapped"),
                )
            }
            // Built-in items take label, group, type and testable from the code, so changes to
            // them reach existing installs; the file keeps only whether they are shown and
            // their order and last tap. Items Luca added are kept whole.
            val byKey = stored.associateBy { it.key }
            val builtIn = DEFAULT_ITEMS.map { d ->
                byKey[d.key]?.let { s -> d.copy(visible = s.visible, order = s.order, lastTapped = s.lastTapped) } ?: d
            }
            val defaultKeys = DEFAULT_ITEMS.map { it.key }.toSet()
            items = builtIn + stored.filter { it.key !in defaultKeys }
        }
        root.optJSONObject("days")?.let { obj ->
            for (date in obj.keys()) {
                val d = obj.getJSONObject(date)
                val v = d.optJSONObject("values")
                val values = v?.keys()?.asSequence()?.associateWith { v.getInt(it) } ?: emptyMap()
                val zone = if (d.isNull("zone")) null else d.getString("zone")
                days[LocalDate.parse(date)] = DayRecord(d.optBoolean("aphtha", false), values, zone)
            }
        }
    }

    fun refreshToday() {
        today = logDate(LocalDateTime.now())
    }

    /** Called when the app comes to the front. */
    fun onResume() {
        refreshToday()
        resumes++
    }

    /** One tap on `item` for `date`: moves its value on by one, notes the time of the tap and saves. */
    fun tapItem(date: LocalDate, item: Item) {
        val now = System.currentTimeMillis()
        items = items.map { if (it.key == item.key) it.copy(lastTapped = now) else it }
        updateDay(date) { r ->
            r.copy(values = r.values + (item.key to nextValue(item.type, r.values[item.key] ?: 0)))
        }
    }

    /**
     * Applies `change` to the record for `date` and saves. A day that was not logged starts as an
     * empty record stamped with the current time zone; later edits keep the zone it had.
     */
    fun updateDay(date: LocalDate, change: (DayRecord) -> DayRecord) {
        val old = days[date] ?: DayRecord(zone = ZoneId.systemDefault().id)
        val shown = items.filter { it.visible }.associate { it.key to 0 }
        val new = change(old)
        days[date] = new.copy(values = shown + new.values)
        save()
    }

    /** Switches Today between the category and "most recent" orders, and saves. */
    fun switchSort() {
        sortRecent = !sortRecent
        save()
    }

    fun setEvening(minutes: Int) {
        eveningMinutes = minutes
        save()
    }

    /** Turns the next-morning nudge on or off, and saves. */
    fun switchNudge() {
        nudge = !nudge
        save()
    }

    /** Shows or hides an item on Today. Hidden items keep their history. */
    fun setVisible(key: String, visible: Boolean) {
        items = items.map { if (it.key == key) it.copy(visible = visible) else it }
        save()
    }

    /** Adds a yes/no item named `label` at the end of `group`, shown on Today. */
    fun addItem(label: String, group: String) {
        items = items + newItem(items, label, group)
        save()
    }

    /**
     * Deletes the log: every day, and when each item was last tapped. What is tracked (shown,
     * hidden and added items) and the reminder settings stay.
     */
    fun deleteAll() {
        days.clear()
        items = items.map { it.copy(lastTapped = null) }
        save()
    }

    private fun save() {
        val text = toJson().toString()
        writer.execute {
            val out = file.startWrite()
            try {
                out.write(text.encodeToByteArray())
                file.finishWrite(out)
            } catch (e: Exception) {
                file.failWrite(out)
                throw e
            }
        }
    }

    private fun toJson(): JSONObject {
        val root = JSONObject()
        root.put("version", 1)
        root.put(
            "settings",
            JSONObject().put("sortRecent", sortRecent).put("eveningMinutes", eveningMinutes).put("nudge", nudge),
        )
        root.put("items", org.json.JSONArray().apply {
            for (it in items) put(
                JSONObject()
                    .put("key", it.key).put("label", it.label).put("group", it.group)
                    .put("type", it.type.code).put("visible", it.visible).put("order", it.order)
                    .put("testable", it.testable?.code ?: JSONObject.NULL)
                    .put("lastTapped", it.lastTapped ?: JSONObject.NULL)
            )
        })
        root.put("days", JSONObject().apply {
            for (date in days.keys.sorted()) {
                val r = days.getValue(date)
                put(
                    date.toString(),
                    JSONObject().put("aphtha", r.aphtha).put("values", JSONObject(r.values))
                        .put("zone", r.zone ?: JSONObject.NULL),
                )
            }
        })
        return root
    }
}
