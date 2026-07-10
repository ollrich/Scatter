package app.scatterto.data.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test fun splitsErrorNameAndDetail() {
        val (name, detail) = parseErrorParts("""{"error":"ExpiredToken","message":"Token has expired"}""")
        assertEquals("ExpiredToken", name)
        assertEquals("Token has expired", detail)
    }

    // atproto meldet abgelaufene Access-Token als 400/ExpiredToken, NICHT als 401 (§12.1 Nr. 4).
    @Test fun expiredTokenAs400CountsAsAuthExpired() {
        assertTrue(ApiError(400, "ExpiredToken", "Token has expired").isAuthExpired)
    }

    @Test fun plain401CountsAsAuthExpired() {
        assertTrue(ApiError(401, null, null).isAuthExpired)
    }

    @Test fun genuineBadRequestIsNotAuthExpired() {
        assertFalse(ApiError(400, "InvalidRequest", "Record is invalid").isAuthExpired)
    }

    @Test fun readableCombinesStatusNameAndDetail() {
        assertEquals(
            "HTTP 400 – ExpiredToken: Token has expired",
            ApiError(400, "ExpiredToken", "Token has expired").readable,
        )
        assertEquals("HTTP 500", ApiError(500, null, null).readable)
    }
}
