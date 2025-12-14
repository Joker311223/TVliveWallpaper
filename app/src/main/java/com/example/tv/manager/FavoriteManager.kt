package com.example.tv.manager

import android.content.Context
import android.util.Log
import com.example.tv.model.VideoSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoriteManager(private val context: Context) {
    
    private val TAG = "FavoriteManager"
    private val PREFS_NAME = "favorites"
    private val FAVORITES_KEY = "favorite_videos"
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val favoriteIds = mutableSetOf<String>()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        try {
            val json = prefs.getString(FAVORITES_KEY, "[]")
            val type = object : TypeToken<List<String>>() {}.type
            val ids: List<String> = gson.fromJson(json, type)
            favoriteIds.clear()
            favoriteIds.addAll(ids)
            Log.d(TAG, "Loaded ${favoriteIds.size} favorites")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorites", e)
            favoriteIds.clear()
        }
    }
    
    private fun saveFavorites() {
        try {
            val json = gson.toJson(favoriteIds.toList())
            prefs.edit().putString(FAVORITES_KEY, json).apply()
            Log.d(TAG, "Saved ${favoriteIds.size} favorites")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorites", e)
        }
    }
    
    fun toggleFavorite(video: VideoSource): Boolean {
        return if (isFavorite(video)) {
            removeFavorite(video)
            false
        } else {
            addFavorite(video)
            true
        }
    }
    
    fun addFavorite(video: VideoSource) {
        if (favoriteIds.add(video.id)) {
            saveFavorites()
            Log.d(TAG, "Added favorite: ${video.title}")
        }
    }
    
    fun removeFavorite(video: VideoSource) {
        if (favoriteIds.remove(video.id)) {
            saveFavorites()
            Log.d(TAG, "Removed favorite: ${video.title}")
        }
    }
    
    fun isFavorite(video: VideoSource): Boolean {
        return favoriteIds.contains(video.id)
    }
    
    fun getFavoriteIds(): Set<String> {
        return favoriteIds.toSet()
    }
    
    fun clearAllFavorites() {
        favoriteIds.clear()
        saveFavorites()
        Log.d(TAG, "Cleared all favorites")
    }
    
    fun getFavoriteCount(): Int {
        return favoriteIds.size
    }
}
