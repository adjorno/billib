package com.m14n.billib.data.billboard.model.consistency

import org.junit.Test

class TrackConsistencyCheckerTest {

    @Test
    fun `test Alex O'Neal`() {
        val actual = Artist.fromRawValue("Alexander O Neal")
        val expected = Artist.fromRawValue("Alexander O'Neal")

        assert(actual == expected)
    }

    @Test
    fun `test The You Know Who Group!`() {
        val actual = Artist.fromRawValue("The You Know Who Group!")
        val expected = Artist.fromRawValue("The \"You Know Who\" Group!")

        assert(actual == expected)
    }

    @Test
    fun `test Sheila E`() {
        val actual = Artist.fromRawValue("Sheila E")
        val expected = Artist.fromRawValue("Sheila E.")

        assert(actual == expected)
    }

    @Test
    fun `test Alison Krauss + Union Station`() {
        val actual = Artist.fromRawValue("Alison Krauss + Union Station")
        val expected = Artist.fromRawValue("Alison Krauss & Union Station")

        assert(actual == expected)
    }

    @Test
    fun `test Oran Juice Jones`() {
        val actual = Artist.fromRawValue("Oran 'Juice' Jones")
        val expected = Artist.fromRawValue("Oran Juice Jones")

        assert(actual == expected)
    }

    @Test
    fun `test Shabba Ranks (Featuring Maxi Priest)`() {
        val actual = Artist.fromRawValue("Shabba Ranks (Featuring Maxi Priest)")
        val expected = Artist.fromRawValue("Shabba Ranks/Maxi Priest")

        assert(actual == expected)
    }

    @Test
    fun `test Master P Feat Fiend, Silkk The Shocker, Mia X`() {
        val actual = Artist.fromRawValue("Master P Feat. Fiend, Silkk The Shocker, Mia X")
        val expected = Artist.fromRawValue("Master P Feat. Fiend, Silkk The Shocker, Mia X & Mystikal")

        assert(actual == expected)
    }

    @Test
    fun `test Eminem Featuring Dr Dre & 50 Cent`() {
        val actual = Artist.fromRawValue("Eminem Featuring Dr. Dre & 50 Cent")
        val expected = Artist.fromRawValue("Eminem, Dr. Dre & 50 Cent")

        assert(actual == expected)
    }

    @Test
    fun `test Victoria's Secret`() {
        val actual = Title.fromRawValue("Victoria's Secret")
        val expected = Title.fromRawValue("Victoria’s Secret")

        assert(actual == expected)
    }

    @Test
    fun `test Never Leave You - Uh Ooh, Uh Oooh!`() {
        val actual = Title.fromRawValue("Never Leave You - Uh Ooh, Uh Oooh!")
        val expected = Title.fromRawValue("Never Leave You - Uh Oooh, Uh Oooh!")

        assert(actual == expected)
    }
}