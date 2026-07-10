package app.scatterto.data.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiErrorsTest {

    @Test fun parsesAtprotoErrorShape() {
        val body = """{"error":"InvalidRequest","message":"Invalid app.bsky.feed.post record"}"""
        assertEquals("InvalidRequest: Invalid app.bsky.feed.post record", parseApiError(body))
    }

    @Test fun parsesOpenAiErrorShape() {
        val body = """{"error":{"message":"The model 'x' does not exist","type":"invalid_request_error"}}"""
        assertEquals("The model 'x' does not exist", parseApiError(body))
    }

    @Test fun returnsNullForNonJsonOrEmpty() {
        assertNull(parseApiError(""))
        assertNull(parseApiError("<html>502</html>"))
    }
}
