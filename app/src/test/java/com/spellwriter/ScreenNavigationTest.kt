package com.spellwriter

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Screen navigation sealed class.
 * Validates that the navigation state structure works correctly.
 */
class ScreenNavigationTest {

    @Test
    fun `Screen Home creates correct instance`() {
        val screen: Screen = Screen.Home
        assertTrue("Screen.Home should be instance of Screen", screen is Screen)
        assertTrue("Screen.Home should be instance of Screen.Home", screen is Screen.Home)
    }

    @Test
    fun `Screen Game creates correct instance`() {
        val screen: Screen = Screen.Game
        assertTrue("Screen.Game should be instance of Screen", screen is Screen)
        assertTrue("Screen.Game should be instance of Screen.Game", screen is Screen.Game)
    }

    @Test
    fun `Screen Home and Game are different instances`() {
        val home: Screen = Screen.Home
        val game: Screen = Screen.Game
        assertNotEquals("Home and Game should be different screens", home, game)
    }

    @Test
    fun `Screen Home singleton behavior`() {
        val home1 = Screen.Home
        val home2 = Screen.Home
        assertSame("Screen.Home should be singleton", home1, home2)
    }

    @Test
    fun `Screen Game singleton behavior`() {
        val game1 = Screen.Game
        val game2 = Screen.Game
        assertSame("Screen.Game should be singleton", game1, game2)
    }
}
