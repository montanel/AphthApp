package ch.luca.aphthapp

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DataTest {
    private fun cycle(type: ItemType, taps: Int): List<Int> =
        generateSequence(0) { nextValue(type, it) }.take(taps + 1).toList()

    @Test
    fun tapsCycleThroughValues() {
        assertEquals(listOf(0, 1, 0), cycle(ItemType.YES_NO, 2))
        assertEquals(listOf(0, 1, 2, 3, 4, 0), cycle(ItemType.COUNT, 5))
        assertEquals(listOf(0, 1, 2, 0), cycle(ItemType.TIMES, 3))
        assertEquals(listOf(0, 1, 2, 0), cycle(ItemType.LEVEL, 3))
        assertEquals(listOf(0, 1, 2, 0), cycle(ItemType.THREE_STATE, 3))
    }

    @Test
    fun defaultItemsMatchTheSpec() {
        val shown = byCategory(DEFAULT_ITEMS)
        assertEquals(GROUPS, shown.map { it.first })
        assertEquals(18, shown.sumOf { it.second.size })
        assertEquals(listOf("Tea", "Coffee", "Alcohol"), shown[0].second.map { it.label })
        assertEquals(22, DEFAULT_ITEMS.map { it.key }.toSet().size)
    }

    @Test
    fun mostRecentPutsTheLastTappedLast() {
        val tapped = mapOf("nuts" to 300L, "b" to 100L, "floss" to 200L)
        val items = DEFAULT_ITEMS.map { it.copy(lastTapped = tapped[it.key]) }
        val order = byMostRecent(items).map { it.key }
        assertEquals(18, order.size)
        assertEquals(listOf("b", "floss", "nuts"), order.takeLast(3))
        // Never-tapped items come first, in category order.
        assertEquals(listOf("tea", "coffee", "alcohol", "banana", "choc"), order.take(5))
        // With no taps at all, the order is the category order.
        assertEquals("tea", byMostRecent(DEFAULT_ITEMS).first().key)
    }

    private val nov = YearMonth.of(2026, 11)
    private fun log(vararg aphtha: Int, plain: List<Int> = emptyList()): Map<LocalDate, DayRecord> =
        aphtha.associate { nov.atDay(it) to DayRecord(aphtha = true) } +
            plain.associate { nov.atDay(it) to DayRecord() }

    @Test
    fun episodesBridgeUpToTwoAphthaFreeDays() {
        // 4, 5, then 8 (two days between) is one episode; 12 (three days after 8) starts another.
        val eps = episodes(log(4, 5, 8, 12, 13))
        assertEquals(listOf(Episode(nov.atDay(4), nov.atDay(8)), Episode(nov.atDay(12), nov.atDay(13))), eps)
    }

    @Test
    fun streakCountsDaysFreeOrDaysIntoAnEpisode() {
        val days = log(4, 5, 6, plain = listOf(7, 8, 9, 10))
        val eps = episodes(days)
        assertEquals(4 to false, currentStreak(days, eps, nov.atDay(10)))
        // On day 6 itself, the episode is on: day 3 of it.
        assertEquals(3 to true, currentStreak(days, eps, nov.atDay(6)))
        // Day 7 not logged yet, aphtha yesterday: still on, day 4.
        val partial = log(4, 5, 6)
        assertEquals(4 to true, currentStreak(partial, episodes(partial), nov.atDay(7)))
        assertEquals(null, currentStreak(log(plain = listOf(1, 2)), emptyList(), nov.atDay(2)))
    }

    @Test
    fun medianGapBetweenEpisodeStarts() {
        val eps = listOf(1, 31, 71, 101).map { Episode(LocalDate.ofEpochDay(it.toLong()), LocalDate.ofEpochDay(it.toLong())) }
        assertEquals(30.0, medianGap(eps)!!, 0.0)
        assertEquals(null, medianGap(eps.take(1)))
    }

    @Test
    fun monthStripRunsAreClippedToTheMonth() {
        val days = log(29, 30, plain = listOf(1, 2, 3, 10)) +
            mapOf(LocalDate.of(2026, 12, 1) to DayRecord(aphtha = true))
        val (logged, aphtha) = monthStrip(nov, days, episodes(days))
        assertEquals(listOf(1..3, 10..10, 29..30), logged)
        assertEquals(listOf(29..30), aphtha)
    }

    @Test
    fun newItemKeysAreUnique() {
        val first = newItem(DEFAULT_ITEMS, "Orange juice", "Drinks")
        assertEquals("my_orange_juice", first.key)
        assertEquals(3, first.order)
        assertEquals("my_orange_juice_2", newItem(DEFAULT_ITEMS + first, "Orange  juice!", "Drinks").key)
    }

    @Test
    fun csvHasOneRowPerDay() {
        val items = DEFAULT_ITEMS.take(2)
        val days = mapOf(
            nov.atDay(2) to DayRecord(false, mapOf("tea" to 2), "America/New_York"),
            nov.atDay(1) to DayRecord(true, mapOf("tea" to 1, "coffee" to 0)),
        )
        assertEquals(
            "date,aphtha,zone,tea,coffee\n2026-11-01,1,,1,0\n2026-11-02,0,America/New_York,2,\n",
            toCsv(items, days),
        )
    }

    @Test
    fun theLogDayStartsAtTwoInTheMorning() {
        val nov24 = LocalDate.of(2026, 11, 24)
        assertEquals(nov24.minusDays(1), logDate(nov24.atTime(1, 59)))
        assertEquals(nov24, logDate(nov24.atTime(2, 0)))
        assertEquals(nov24, logDate(nov24.atTime(23, 59)))
    }
}
