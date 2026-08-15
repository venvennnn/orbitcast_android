package app.orbitcast.data.api

import app.orbitcast.data.api.models.CreateFeedBody
import app.orbitcast.data.api.models.Episode
import app.orbitcast.data.api.models.Feed
import app.orbitcast.data.api.models.FeedDetail
import app.orbitcast.data.api.models.FeedSummary
import app.orbitcast.data.api.models.Health
import app.orbitcast.data.api.models.PatchFeedBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Client contract for the OrbitCast control-plane API.
 *
 * Live today: [health], [feeds], [feed], [createFeed], [refreshFeed], [retryEpisode].
 * B5 (PATCH/DELETE) is wired so the screens can call it; the current API
 * returns 404/405 and the UI surfaces that as "not on the API yet".
 */
interface OrbitCastApi {
    @GET("health")
    suspend fun health(): Health

    @GET("feeds")
    suspend fun feeds(): List<FeedSummary>

    @GET("feeds/{id}")
    suspend fun feed(@Path("id") id: String): FeedDetail

    @POST("feeds")
    suspend fun createFeed(@Body body: CreateFeedBody): Feed

    @PATCH("feeds/{id}")
    suspend fun patchFeed(@Path("id") id: String, @Body body: PatchFeedBody): Feed

    @DELETE("feeds/{id}")
    suspend fun deleteFeed(@Path("id") id: String)

    @POST("feeds/{id}/refresh")
    suspend fun refreshFeed(@Path("id") id: String): Episode

    @POST("episodes/{id}/retry")
    suspend fun retryEpisode(@Path("id") id: String): Episode
}
