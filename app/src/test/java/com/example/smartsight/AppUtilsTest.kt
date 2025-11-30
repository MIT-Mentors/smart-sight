package com.example.smartsight

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class AppFunctionsTest {

    @Test
    fun testBatteryPercent() {
        // Arrange
        val context = mockk<Context>()
        val batteryManager = mockk<BatteryManager>()
        every { context.getSystemService(Context.BATTERY_SERVICE) } returns batteryManager
        every { batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 83

        // Act
        val result = batteryPercent(context)

        // Assert
        assertEquals(83, result)
    }

    @Test
    fun testIsInternetConnected_newApiAndLegacyMocked() {
        // Arrange
        val context = mockk<Context>()
        val cm = mockk<ConnectivityManager>()
        val network = mockk<Network>()
        val caps = mockk<NetworkCapabilities>()
        val legacyInfo = mockk<NetworkInfo>()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns cm
        every { cm.activeNetwork } returns network
        every { cm.getNetworkCapabilities(network) } returns caps
        every { caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { cm.activeNetworkInfo } returns legacyInfo
        every { legacyInfo.isConnected } returns true

        // Act
        val result = isInternetConnected(context)

        // Assert
        assertTrue(result)
    }

    @Test
    fun testIsBluetoothReadyAndDevicePaired_robolectricFriendly() {
        // Arrange
        mockkStatic(ContextCompat::class)
        mockkStatic(BluetoothAdapter::class)
        val context = mockk<Context>()
        val bluetoothManager = mockk<BluetoothManager>()
        val adapter = mockk<BluetoothAdapter>()
        val device = mockk<BluetoothDevice>()
        every { context.getSystemService(Context.BLUETOOTH_SERVICE) } returns bluetoothManager
        every { bluetoothManager.adapter } returns adapter
        every { adapter.isEnabled } returns true
        every { adapter.bondedDevices } returns setOf(device)
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        } returns PackageManager.PERMISSION_GRANTED
        every { BluetoothAdapter.getDefaultAdapter() } returns adapter

        // Act
        val result = isBluetoothReadyAndDevicePaired(context)

        // Assert
        assertTrue(result)
    }
}
