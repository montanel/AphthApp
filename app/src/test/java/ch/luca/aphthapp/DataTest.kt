package ch.luca.aphthapp

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

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

    @Test
    fun theLogDayStartsAtFourInTheMorning() {
        val nov24 = LocalDate.of(2026, 11, 24)
        assertEquals(nov24.minusDays(1), logDate(nov24.atTime(3, 59)))
        assertEquals(nov24, logDate(nov24.atTime(4, 0)))
        assertEquals(nov24, logDate(nov24.atTime(23, 59)))
    }
}
