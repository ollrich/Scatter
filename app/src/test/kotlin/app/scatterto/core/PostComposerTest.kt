package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PostComposerTest {

    @Test fun composesTextHashtagUrl() {
        assertEquals(
            "Hab was gefunden\n\n#Klima https://a.de/x",
            composePost("Hab was gefunden", "#Klima", "https://a.de/x"),
        )
    }

    @Test fun omitsEmptyHashtag() {
        assertEquals(
            "Nur Text\n\nhttps://a.de/x",
            composePost("Nur Text", "", "https://a.de/x"),
        )
    }

    @Test fun trimsSurroundingWhitespace() {
        assertEquals(
            "Text\n\n#Tag https://a.de",
            composePost("  Text  ", " #Tag ", " https://a.de "),
        )
    }
}
