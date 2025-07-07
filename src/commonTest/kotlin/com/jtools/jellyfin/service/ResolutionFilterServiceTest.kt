package com.jtools.jellyfin.service

import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.config.JellyfinConfig
import com.jtools.jellyfin.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ResolutionFilterServiceTest {
    
    private fun createMockApiClient(): JellyfinApiClient {
        val config = JellyfinConfig("http://localhost:8096", "test-token")
        return JellyfinApiClient(config)
    }
    
    private fun createTestMovie(name: String, width: Int?, height: Int?): JellyfinMovie {
        return JellyfinMovie(
            id = "test-id-$name",
            name = name,
            width = width,
            height = height
        )
    }
    
    @Test
    fun testResolutionCategorization() {
        val service = ResolutionFilterService(createMockApiClient())
        
        // 测试分辨率分类
        val testCases = listOf(
            Triple(640, 480, "480p及以下"),
            Triple(1280, 720, "720p"),
            Triple(1920, 1080, "1080p"),
            Triple(3840, 2160, "4K"),
            Triple(7680, 4320, "超高清")
        )
        
        testCases.forEach { (width, height, expected) ->
            val actual = service.categorizeResolution(width, height)
            assertEquals(expected, actual, "Resolution ${width}x${height} should be categorized as $expected")
        }
    }
    
    @Test
    fun testResolutionInfoExtraction() {
        val service = ResolutionFilterService(createMockApiClient())
        
        // 测试从电影对象提取分辨率信息
        val movie1080p = createTestMovie("Test Movie 1080p", 1920, 1080)
        val movieUnknown = createTestMovie("Test Movie Unknown", null, null)
        
        val info1080p = service.getMovieResolutionInfo(movie1080p)
        assertEquals(1920, info1080p.width)
        assertEquals(1080, info1080p.height)
        assertEquals("1920x1080", info1080p.displayResolution)
        assertEquals("movie", info1080p.source)
        
        val infoUnknown = service.getMovieResolutionInfo(movieUnknown)
        assertEquals(null, infoUnknown.width)
        assertEquals(null, infoUnknown.height)
        assertEquals("未知", infoUnknown.displayResolution)
        assertEquals("unknown", infoUnknown.source)
    }
    
    @Test
    fun testLowResolutionDetection() {
        val service = ResolutionFilterService(createMockApiClient())
        
        // 测试低分辨率检测逻辑
        val resolution720p = ResolutionFilterService.ResolutionInfo(
            width = 1280,
            height = 720,
            displayResolution = "1280x720",
            isLowResolution = false,
            source = "movie"
        )
        
        val resolution1080p = ResolutionFilterService.ResolutionInfo(
            width = 1920,
            height = 1080,
            displayResolution = "1920x1080",
            isLowResolution = false,
            source = "movie"
        )
        
        val resolutionUnknown = ResolutionFilterService.ResolutionInfo(
            width = null,
            height = null,
            displayResolution = "未知",
            isLowResolution = false,
            source = "unknown"
        )
        
        // 测试1080p以下筛选
        val maxWidth1080p = 1920
        val maxHeight1080p = 1080
        
        assertTrue(service.isLowResolution(resolution720p, maxWidth1080p, maxHeight1080p, false))
        assertFalse(service.isLowResolution(resolution1080p, maxWidth1080p, maxHeight1080p, false))
        assertFalse(service.isLowResolution(resolutionUnknown, maxWidth1080p, maxHeight1080p, false))
        assertTrue(service.isLowResolution(resolutionUnknown, maxWidth1080p, maxHeight1080p, true))
    }
    
    @Test
    fun testResolutionTypeMaxValues() {
        // 测试分辨率类型的最大值设置
        val testCases = mapOf(
            ResolutionFilterService.ResolutionType.SD_480P to Pair(854, 480),
            ResolutionFilterService.ResolutionType.HD_720P to Pair(1280, 720),
            ResolutionFilterService.ResolutionType.HD_1080P to Pair(1920, 1080),
            ResolutionFilterService.ResolutionType.UHD_4K to Pair(3840, 2160)
        )
        
        testCases.forEach { (type, expected) ->
            val actual = ResolutionFilterService.ResolutionType.getMaxResolution(type)
            assertEquals(expected, actual, "Resolution type $type should have max resolution $expected")
        }
    }
}