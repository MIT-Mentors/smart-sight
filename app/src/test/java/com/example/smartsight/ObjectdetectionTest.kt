package com.example.smartsight

import org.junit.Assert.*
import org.junit.Test

class DetectionUtilsTest {

    @Test
    fun `returns no objects message when empty`() {
        val result = formatDetectionResults(emptyList())
        assertEquals("No highly confident objects detected.", result)
    }

    @Test
    fun `formats single detected object correctly`() {
        val result = formatDetectionResults(listOf("lion" to 0.95f))
        assertEquals("Detected objects are cat (95%)", result)
    }

    @Test
    fun `formats multiple detected objects correctly`() {
        val result = formatDetectionResults(listOf("dog" to 0.85f, "person" to 0.90f))
        assertEquals("Detected objects are dog (85%), person (90%)", result)
    }
}
