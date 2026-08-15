package app.orbitcast.data.repo

import app.orbitcast.data.api.ApiFactory
import app.orbitcast.data.api.models.CreateFeedBody
import app.orbitcast.data.api.models.Episode
import app.orbitcast.data.api.models.Feed
import app.orbitcast.data.api.models.FeedDetail
import app.orbitcast.data.api.models.FeedSummary
import app.orbitcast.data.api.models.Health
import app.orbitcast.data.api.models.PatchFeedBody
import app.orbitcast.data.session.SessionStore
import kotlinx.coroutines.flow.first

class FeedRepository(
    private val apiFactory: ApiFactory,
    private val session: SessionStore,
) {
    private suspend fun api() = apiFactory.api(session.apiBaseUrl.first())

    suspend fun health(): Health = api().health()

    suspend fun feeds(): List<FeedSummary> = api().feeds()

    suspend fun feed(id: String): FeedDetail = api().feed(id)

    suspend fun create(body: CreateFeedBody): Feed = api().createFeed(body)

    suspend fun patch(id: String, body: PatchFeedBody): Feed = api().patchFeed(id, body)

    suspend fun delete(id: String) = api().deleteFeed(id)

    suspend fun refresh(id: String): Episode = api().refreshFeed(id)

    suspend fun retry(episodeId: String): Episode = api().retryEpisode(episodeId)

    suspend fun rssUrl(slug: String): String {
        val base = session.apiBaseUrl.first()
        return app.orbitcast.util.Urls.rss(base, slug)
    }
}
