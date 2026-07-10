package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PostComposerTest {

    @Test fun ordersTextThenUrlThenHashtags() {
        assertEquals(
            "Bei #NDR über #klima. Kurzer Satz.\n\nhttps://a.de/x\n\n#moor",
            composePost("Bei #NDR über #klima. Kurzer Satz.", listOf("#moor"), "https://a.de/x"),
        )
    }

    @Test fun omitsEmptyHashtagList() {
        assertEquals(
            "Nur Text\n\nhttps://a.de/x",
            composePost("Nur Text", emptyList(), "https://a.de/x"),
        )
    }

    @Test fun joinsMultipleExtraHashtags() {
        assertEquals(
            "Text\n\nhttps://a.de\n\n#a #b",
            composePost("Text", listOf("#a", "#b"), "https://a.de"),
        )
    }

    @Test fun trimsSurroundingWhitespace() {
        assertEquals(
            "Text\n\nhttps://a.de\n\n#Tag",
            composePost("  Text  ", listOf(" #Tag "), " https://a.de "),
        )
    }
}
