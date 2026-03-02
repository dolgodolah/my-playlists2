package com.garfield.myplaylists.adapter.out.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class YoutubeApiClientTest {

    @Autowired
    lateinit var youtubeApiClient: YoutubeApiClient

    @Test
    fun `search videos with valid keyword and limit`() {
        val keyword = "아이유 밤편지"
        val limit = 10
        val response = youtubeApiClient.searchVideos(keyword, limit)
        assertNotNull(response)
        assertTrue(response.size <= limit)
    }
}
