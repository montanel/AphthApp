package ch.luca.aphthapp

import java.time.LocalDate
import java.time.LocalDateTime

/** How an item is logged and what its stored value means. */
enum class ItemType(val code: String) {
    /** 0 no, 1 yes. */
    YES_NO("yesno"),

    /** Cups: 0 to 4, where 4 means "4 or more". */
    COUNT("count"),

    /** Times in the day, 0 to 2 (brushing). */
    TIMES("times"),

    /** Stress level: 0 none, 1 some, 2 a lot. */
    LEVEL("level"),

    /** SLS toothpaste: 0 no, 1 yes, 2 don't know. */
    THREE_STATE("three");

    companion object {
        fun of(code: String): ItemType = entries.first { it.code == code }
    }
}

/** Whether a later test (DECISIONS D-019) may add or remove the item. Unused in v1. */
enum class Testable(val code: String) {
    REMOVE_ONLY("remove"), ADD_OR_REMOVE("add_remove"), WATCH_ONLY("watch");

    companion object {
        fun of(code: String?): Testable? = entries.firstOrNull { it.code == code }
    }
}

/**
 * One thing that can be ticked on Today.
 *
 * `key` is what the log stores, so it must never change once days use it. `order` is the
 * position within its group. `lastTapped` is when a tap last changed its value, on any day
 * (epoch milliseconds), or null if it never was; it drives the "most recent" order.
 */
data class Item(
    val key: String,
    val label: String,
    val group: String,
    val type: ItemType,
    val visible: Boolean = true,
    val order: Int = 0,
    val testable: Testable? = null,
    val lastTapped: Long? = null,
)

/**
 * What was logged for one day. A date with no record is "not logged", which is different from
 * a record with nothing ticked.
 *
 * `values` holds a raw value for every item that was shown when the day was saved (0 when not
 * ticked); an item missing from it was hidden or did not exist yet.
 *
 * `zone` is the phone's time zone when the record was created (e.g. "Asia/Seoul"), so travel
 * days can be found later; null for records made before it was saved.
 */
data class DayRecord(
    val aphtha: Boolean = false,
    val values: Map<String, Int> = emptyMap(),
    val zone: String? = null,
)

/** Group names in display order. */
val GROUPS = listOf("Drinks", "Food", "Mouth care", "Body", "Supplements")

/** The items a new install starts with (DECISIONS D-007). */
val DEFAULT_ITEMS: List<Item> = run {
    val rows = listOf(
        Triple("tea", "Tea", "Drinks") to ItemType.COUNT,
        Triple("coffee", "Coffee", "Drinks") to ItemType.COUNT,
        Triple("alcohol", "Alcohol", "Drinks") to ItemType.YES_NO,
        Triple("banana", "Banana", "Food") to ItemType.YES_NO,
        Triple("nuts", "Nuts", "Food") to ItemType.YES_NO,
        Triple("choc", "Chocolate", "Food") to ItemType.YES_NO,
        Triple("rawfruit", "Raw apple/carrot", "Food") to ItemType.YES_NO,
        Triple("spicy", "Spicy", "Food") to ItemType.YES_NO,
        Triple("acidic", "Citrus/tomato", "Food") to ItemType.YES_NO,
        Triple("crunchy", "Crunchy food", "Food") to ItemType.YES_NO,
        Triple("gum", "Gum/mints", "Food") to ItemType.YES_NO,
        Triple("brush", "Brushed", "Mouth care") to ItemType.TIMES,
        Triple("water", "Water floss", "Mouth care") to ItemType.YES_NO,
        Triple("floss", "Floss", "Mouth care") to ItemType.YES_NO,
        Triple("sls", "SLS toothpaste", "Mouth care") to ItemType.THREE_STATE,
        Triple("mouthwash", "Mouthwash", "Mouth care") to ItemType.YES_NO,
        Triple("bite", "Bit mouth", "Body") to ItemType.YES_NO,
        Triple("stress", "Stress", "Body") to ItemType.LEVEL,
        Triple("sleep", "Bad sleep", "Body") to ItemType.YES_NO,
        Triple("ill", "Feeling ill", "Body") to ItemType.YES_NO,
        Triple("dentist", "Dental visit", "Body") to ItemType.YES_NO,
        Triple("b", "B complex", "Supplements") to ItemType.YES_NO,
    )
    val hidden = setOf("crunchy", "gum", "mouthwash", "dentist")
    val counters = mutableMapOf<String, Int>()
    rows.map { (t, type) ->
        val (key, label, group) = t
        val order = counters.getOrDefault(group, 0).also { counters[group] = it + 1 }
        Item(
            key, label, group, type,
            visible = key !in hidden,
            order = order,
            testable = if (key == "rawfruit") Testable.WATCH_ONLY else null,
        )
    }
}

/** The value after one more tap on an item that now holds `value`. */
fun nextValue(type: ItemType, value: Int): Int = when (type) {
    ItemType.YES_NO -> if (value == 0) 1 else 0
    ItemType.COUNT -> if (value >= 4) 0 else value + 1
    ItemType.TIMES, ItemType.LEVEL, ItemType.THREE_STATE -> (value + 1) % 3
}

/** Hour of the morning when a new log day starts; before it, the previous day is still "today". */
const val DAY_START_HOUR = 4

/** The log day that `now` (local wall-clock time) belongs to. */
fun logDate(now: LocalDateTime): LocalDate = now.minusHours(DAY_START_HOUR.toLong()).toLocalDate()

/** Visible items grouped for the "by category" view: groups in [GROUPS] order, then by `order`. */
fun byCategory(items: List<Item>): List<Pair<String, List<Item>>> =
    GROUPS.mapNotNull { g ->
        val inGroup = items.filter { it.visible && it.group == g }.sortedBy { it.order }
        if (inGroup.isEmpty()) null else g to inGroup
    }

/**
 * Visible items for the "most recent" view: never-tapped items first, in category order, then
 * the others from the longest ago to the most recently tapped, which comes last.
 */
fun byMostRecent(items: List<Item>): List<Item> {
    val (tapped, never) = byCategory(items).flatMap { it.second }.partition { it.lastTapped != null }
    return never + tapped.sortedBy { it.lastTapped }
}
