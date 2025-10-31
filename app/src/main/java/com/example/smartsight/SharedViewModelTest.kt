package com.example.smartsight

import android.app.Application
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SharedViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: SharedViewModel

    @Before
    fun setup() {
        application = mock(Application::class.java)
        viewModel = SharedViewModel(application)
    }

    @Test
    fun testUpdateResponseText_updatesStateCorrectly() = runTest {
        // Arrange
        val expectedText = "SmartSight Ready"

        // Act
        viewModel.updateResponseText(expectedText)

        // Assert
        assertEquals(expectedText, viewModel.responseText.value)
    }

    @Test
    fun testSendMessage_capture_setsRequestingImageText() = runTest {
        // Arrange
        val inputMessage = "capture"

        // Act
        viewModel.sendMessage(inputMessage)

        // Assert
        assertEquals("Requesting image...", viewModel.responseText.value)
    }
}
