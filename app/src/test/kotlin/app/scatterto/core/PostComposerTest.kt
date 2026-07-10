package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PostComposerTest {

    @Test fun composesTextExtraHashtagsUrl() {
        assertEquals(
            "Bei #NDR über #klima. Kurzer Satz.\n\n#moor https://a.de/x",
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
            "Text\n\n#a #b https://a.de",
            composePost("Text", listOf("#a", "#b"), "https://a.de"),
        )
    }

    @Test fun trimsSurroundingWhitespace() {
        assertEquals(
            "Text\n\n#Tag https://a.de",
            composePost("  Text  ", listOf(" #Tag "), " https://a.de "),
        )
    }
}
