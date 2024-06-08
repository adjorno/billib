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
    fun `test The Righteous Brothers`() {
        val actual = Artist.fromRawValue("Righteous Brothers")
        val expected = Artist.fromRawValue("The Righteous Brothers")

        assert(actual == expected)
    }

    @Test
    fun `test Too $hort`() {
        val actual = Artist.fromRawValue("Too Short")
        val expected = Artist.fromRawValue("Too \$hort")

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
    fun `test Ye & Ty Dolla $ign Featuring North West`() {
        val actual = Artist.fromRawValue("¥\$: Ye & Ty Dolla \$ign Featuring North West")
        val expected = Artist.fromRawValue("¥\$: Kanye West & Ty Dolla \$ign Featuring North West")

        assert(actual == expected)
    }

    @Test
    fun `test Frank Sinatra With The Orchestra & Chorus Of Gordon Jenkins`() {
        val actual = Artist.fromRawValue("Frank Sinatra With The Orchestra & Chorus Of Gordon Jenkins")
        val expected = Artist.fromRawValue("Frank Sinatra With Orchestra Conducted By Gordon Jenkins")

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
    fun `test Santana Featuring The Product G&B`() {
        val actual = Artist.fromRawValue("Santana Featuring The Product G&B")
        val expected = Artist.fromRawValue("Santana Featuring The Product G")

        assert(actual == expected)
    }

    @Test
    fun `test P Diddy & Ginuwine Featuring Loon, Mario Winans & Tammy Ruggeri,`() {
        val actual = Artist.fromRawValue("P. Diddy & Ginuwine Featuring Loon, Mario Winans & Tammy Ruggeri,")
        val expected = Artist.fromRawValue("P. Diddy & Ginuwine Featuring Loon, Mario Winans & Tammy Ruggieri,")

        assert(actual == expected)
    }

    @Test
    fun `test DJ Khaled Feat Akon, Plies, Young Jeezy, Rick Ross, Ace Hood, Trick Daddy & Lil' Boosie`() {
        val actual = Artist.fromRawValue("DJ Khaled Feat. Akon, Plies, Young Jeezy, Rick Ross, Ace Hood, Trick Daddy & Lil' Boosie")
        val expected = Artist.fromRawValue("DJ Khaled Featuring Akon, Rick Ross, Young Jeezy, Lil' Boosie, Trick Daddy, Ace Hood & Pli")

        assert(actual == expected)
    }

    @Test
    fun `test Eminem Featuring Dr Dre & 50 Cent`() {
        val actual = Artist.fromRawValue("Eminem Featuring Dr. Dre & 50 Cent")
        val expected = Artist.fromRawValue("Eminem, Dr. Dre & 50 Cent")

        assert(actual == expected)
    }

    @Test
    fun `test Living It Up`() {
        val actual = Title.fromRawValue("Living It Up")
        val expected = Title.fromRawValue("Livin' It Up")

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