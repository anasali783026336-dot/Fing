package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DeviceCategory
import com.example.data.local.DeviceEntity
import com.example.data.local.NetPulseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: NetPulseDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, NetPulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun appName_andLocalizedStrings_areConfiguredCorrectly() {
        val appName = context.getString(R.string.app_name)
        assertEquals("NetPulse", appName)
        assertEquals("الأجهزة", context.getString(R.string.tab_devices))
        assertEquals("السرعة", context.getString(R.string.tab_speed))
        assertEquals("الأمان", context.getString(R.string.tab_security))
        assertEquals("الأدوات", context.getString(R.string.tab_tools))
    }

    @Test
    fun roomDatabase_insertsAndUpdatesNetworkDeviceCorrectly() = runBlocking {
        val dao = database.dao()
        val testDevice = DeviceEntity(
            macAddress = "3C:22:FB:91:40:12",
            ipAddress = "192.168.1.14",
            customName = "MacBook Pro 16",
            defaultHostname = "macbook.local",
            vendor = "Apple, Inc.",
            categoryName = DeviceCategory.LAPTOP.name,
            isOnline = true,
            latencyMs = 4
        )
        dao.upsertDevice(testDevice)

        val fetched = dao.getDeviceByMac("3C:22:FB:91:40:12")
        assertNotNull(fetched)
        assertEquals("MacBook Pro 16", fetched?.customName)

        val allDevices = dao.observeAllDevices().first()
        assertTrue(allDevices.any { it.ipAddress == "192.168.1.14" })
    }
}
