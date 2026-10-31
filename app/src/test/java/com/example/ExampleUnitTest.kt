package com.example

import com.example.data.local.DeviceCategory
import com.example.data.local.DeviceEntity
import com.example.data.network.NetworkCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun deviceCategory_mappingAndOpenPortsParsing_isCorrect() {
        val routerDevice = DeviceEntity(
            macAddress = "A4:2B:B0:E8:4C:01",
            ipAddress = "192.168.1.1",
            customName = "الراوتر الرئيسي",
            defaultHostname = "router.gateway.lan",
            vendor = "TP-Link",
            categoryName = DeviceCategory.ROUTER.name,
            isOnline = true,
            latencyMs = 2,
            isGateway = true,
            openPortsCsv = "53, 80, 443, 1900"
        )

        assertEquals(DeviceCategory.ROUTER, routerDevice.category)
        assertEquals(listOf(53, 80, 443, 1900), routerDevice.openPortsList)
    }

    @Test
    fun networkCatalog_identifiesRiskyPortsAndCategoriesCorrectly() {
        val telnetInfo = NetworkCatalog.lookupPortInfo(23)
        assertEquals("Telnet", telnetInfo.serviceName)
        assertTrue(telnetInfo.isRisky)

        val httpsInfo = NetworkCatalog.lookupPortInfo(443)
        assertEquals("HTTPS", httpsInfo.serviceName)
        assertFalse(httpsInfo.isRisky)

        val inferredCamera = NetworkCatalog.inferCategoryFromOpenPorts(
            ports = listOf(80, 554, 8080),
            isGateway = false,
            isSelf = false
        )
        assertEquals(DeviceCategory.CAMERA, inferredCamera)
    }
}
