package com.garfield.myplaylists.adapter.out.client

import com.garfield.myplaylists.domain.song.YoutubeVideo
import com.garfield.myplaylists.usecase.port.out.YoutubeApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import org.springframework.web.util.HtmlUtils

@Component
class YoutubeApiClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${youtube.api.key:}") private val youtubeApiKey: String,
) : YoutubeApi {
    private val restClient = restClientBuilder
        .baseUrl(youtubeApiBaseUrl)
        .build()

    override fun searchVideos(keyword: String, limit: Int): List<YoutubeVideo> {
        if (youtubeApiKey.isBlank()) {
            return emptyList()
        }

        return try {
            val response = restClient.get()
                .uri { builder ->
                    builder
                        .path("/youtube/v3/search")
                        .queryParam("part", "snippet")
                        .queryParam("type", "video")
                        .queryParam("videoEmbeddable", "true")
                        .queryParam("maxResults", limit)
                        .queryParam("key", youtubeApiKey)
                        .queryParam("q", keyword)
                        .build()
                }
                .retrieve()
                .body<YoutubeSearchApiResponse>()

            response?.items.orEmpty()
                .mapNotNull { item ->
                    val videoId = item.id?.videoId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    val title = item.snippet?.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    YoutubeVideo(
                        title = HtmlUtils.htmlUnescape(title),
                        videoId = videoId,
                    )
                }
        } catch (_: RestClientException) {
            throw IllegalStateException("유튜브 검색에 실패했습니다. 잠시 후 다시 시도해주세요.")
        }
    }

    private data class YoutubeSearchApiResponse(
        val items: List<YoutubeSearchApiItem>?,
    )

    private data class YoutubeSearchApiItem(
        val id: YoutubeVideoId?,
        val snippet: YoutubeSnippet?,
    )

    private data class YoutubeVideoId(
        val videoId: String?,
    )

    private data class YoutubeSnippet(
        val title: String?,
    )

    companion object {
        private const val youtubeApiBaseUrl = "https://www.googleapis.com"
    }
}
