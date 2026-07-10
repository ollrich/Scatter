package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class HashtagTest {

    @Test fun addsMissingHash() {
        assertEquals("#Test", normalizeHashtag("Test"))
    }

    @Test fun preservesLowercaseTopic() {
        assertEquals("#klimawandel", normalizeHashtag("#klimawandel"))
    }

    @Test fun preservesAcronymCase() {
        // Eigennamen/Kürzel bleiben unverändert — kein erzwungenes Klein/CamelCase.
        assertEquals("#NDR", normalizeHashtag("NDR"))
        assertEquals("#NDR", normalizeHashtag("#NDR"))
    }

    @Test fun joinsWordsWithoutForcingCase() {
        assertEquals("#klimawandel", normalizeHashtag("klima wandel"))
    }

    @Test fun collapsesDoubleHash() {
        assertEquals("#doppel", normalizeHashtag("##doppel"))
    }

    @Test fun blankBecomesEmpty() {
        assertEquals("", normalizeHashtag("   "))
    }
}
