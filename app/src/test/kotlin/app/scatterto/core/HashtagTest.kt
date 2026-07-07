package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class HashtagTest {

    @Test fun addsMissingHash() {
        assertEquals("#Test", normalizeHashtag("Test"))
    }

    @Test fun keepsSingleWordCase() {
        assertEquals("#klimawandel", normalizeHashtag("#klimawandel"))
    }

    @Test fun joinsMultipleWordsAsCamelCase() {
        assertEquals("#KlimaWandel", normalizeHashtag("klima wandel"))
    }

    @Test fun collapsesDoubleHash() {
        assertEquals("#doppel", normalizeHashtag("##doppel"))
    }

    @Test fun blankBecomesEmpty() {
        assertEquals("", normalizeHashtag("   "))
    }
}
