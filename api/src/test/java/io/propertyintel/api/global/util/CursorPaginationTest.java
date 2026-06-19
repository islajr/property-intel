package io.propertyintel.api.global.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CursorPaginationTest {

    @Test
    void testEncodeAndDecode() {
        Long originalId = 12345L;
        String cursor = CursorPagination.encode(originalId);
        assertNotNull(cursor);
        assertEquals("eyJpZCI6MTIzNDV9", cursor);

        Long decodedId = CursorPagination.decode(cursor);
        assertEquals(originalId, decodedId);
    }

    @Test
    void testDecodeNullOrBlank() {
        assertNull(CursorPagination.decode(null));
        assertNull(CursorPagination.decode(""));
        assertNull(CursorPagination.decode("   "));
    }

    @Test
    void testInvalidCursor() {
        assertThrows(IllegalArgumentException.class, () -> CursorPagination.decode("invalid-base64"));
    }
}
