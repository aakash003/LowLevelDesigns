package org.flipkart.serverallocator;

import org.flipkart.serverallocator.ServerAllocator;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerAllocatorTest {

    @Test
    void testMissingMiddleValue() {
        List<Number> allocated = Arrays.asList(5, 3, 1);
        assertEquals(2, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testMissingInternalValue() {
        List<Number> allocated = Arrays.asList(5, 4, 1, 2);
        assertEquals(3, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testSequentialAllocated() {
        List<Number> allocated = Arrays.asList(3, 2, 1);
        assertEquals(4, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testMissingFirst() {
        List<Number> allocated = Arrays.asList(2, 3);
        assertEquals(1, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testEmptyList() {
        List<Number> allocated = Collections.emptyList();
        assertEquals(1, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testWithFloats() {
        List<Number> allocated = Arrays.asList(1, 1.5, 2, 2.5, 3, 3.5, 4, 5, 5.5);
        assertEquals(6, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testOnlyFloat() {
        List<Number> allocated = Collections.singletonList(2.5);
        assertEquals(1, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testAllPositiveIntegers() {
        List<Number> allocated = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        assertEquals(8, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testNegativeAndZero() {
        List<Number> allocated = Arrays.asList(-1, 0, 1, 2);
        assertEquals(3, ServerAllocator.nextServerNumber(allocated));
    }

    @Test
    void testDuplicateIntegers() {
        List<Number> allocated = Arrays.asList(1, 1, 2, 2, 3, 3);
        assertEquals(4, ServerAllocator.nextServerNumber(allocated));
    }
}
