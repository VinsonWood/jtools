package com.jtools.jellyfin.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JellyfinConfigTest {
    
    @Test
    fun testValidConfig() {
        val config = JellyfinConfig(
            serverUrl = "http://localhost:8096",
            apiToken = "test-token"
        )
        
        assertTrue(config.isValid())
    }
    
    @Test
    fun testInvalidConfigEmptyUrl() {
        val config = JellyfinConfig(
            serverUrl = "",
            apiToken = "test-token"
        )
        
        assertFalse(config.isValid())
    }
    
    @Test
    fun testInvalidConfigEmptyToken() {
        val config = JellyfinConfig(
            serverUrl = "http://localhost:8096",
            apiToken = ""
        )
        
        assertFalse(config.isValid())
    }
    
    @Test
    fun testInvalidConfigNoProtocol() {
        val config = JellyfinConfig(
            serverUrl = "localhost:8096",
            apiToken = "test-token"
        )
        
        assertFalse(config.isValid())
    }
    
    @Test
    fun testFormattedServerUrl() {
        val config1 = JellyfinConfig(
            serverUrl = "http://localhost:8096",
            apiToken = "test-token"
        )
        assertEquals("http://localhost:8096/", config1.formattedServerUrl)
        
        val config2 = JellyfinConfig(
            serverUrl = "http://localhost:8096/",
            apiToken = "test-token"
        )
        assertEquals("http://localhost:8096/", config2.formattedServerUrl)
    }
    
    @Test
    fun testDefaultConfig() {
        val config = JellyfinConfig.default()
        
        assertEquals("http://localhost:8096", config.serverUrl)
        assertEquals("", config.apiToken)
        assertEquals(null, config.userId)
        assertFalse(config.isValid()) // 因为token为空
    }
}
