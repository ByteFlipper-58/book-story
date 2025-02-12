package com.byteflipper.everbook.data.remote

import retrofit2.http.GET
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo

interface GithubAPI {
    @GET("repos/ByteFlipper-58/book-story/releases/latest")
    suspend fun getLatestRelease(): LatestReleaseInfo

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}