package com.example.tv.api

import com.example.tv.model.VideoListResponse
import retrofit2.http.GET

interface VideoApi {
    @GET("test.api")
    suspend fun getVideoList(): VideoListResponse
}
