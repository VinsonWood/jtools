package com.jtools.jellyfin.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JellyfinModelsTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Test
    fun testJellyfinUserSerialization() {
        val user = JellyfinUser(
            id = "test-id",
            name = "Test User",
            serverId = "server-id"
        )
        
        val jsonString = json.encodeToString(user)
        val deserializedUser = json.decodeFromString<JellyfinUser>(jsonString)
        
        assertEquals(user.id, deserializedUser.id)
        assertEquals(user.name, deserializedUser.name)
        assertEquals(user.serverId, deserializedUser.serverId)
    }
    
    @Test
    fun testJellyfinMovieSerialization() {
        val movie = JellyfinMovie(
            id = "movie-id",
            name = "Test Movie",
            originalTitle = "Original Title",
            overview = "Movie overview",
            productionYear = 2023,
            genres = listOf("Action", "Drama"),
            communityRating = 8.5,
            userData = UserData(isFavorite = true)
        )
        
        val jsonString = json.encodeToString(movie)
        val deserializedMovie = json.decodeFromString<JellyfinMovie>(jsonString)
        
        assertEquals(movie.id, deserializedMovie.id)
        assertEquals(movie.name, deserializedMovie.name)
        assertEquals(movie.originalTitle, deserializedMovie.originalTitle)
        assertEquals(movie.productionYear, deserializedMovie.productionYear)
        assertEquals(movie.genres, deserializedMovie.genres)
        assertEquals(movie.communityRating, deserializedMovie.communityRating)
        assertTrue(deserializedMovie.userData?.isFavorite == true)
    }
    
    @Test
    fun testJellyfinPersonSerialization() {
        val person = JellyfinPerson(
            id = "person-id",
            name = "Test Actor",
            type = "Actor",
            role = "Main Character",
            userData = UserData(isFavorite = true)
        )
        
        val jsonString = json.encodeToString(person)
        val deserializedPerson = json.decodeFromString<JellyfinPerson>(jsonString)
        
        assertEquals(person.id, deserializedPerson.id)
        assertEquals(person.name, deserializedPerson.name)
        assertEquals(person.type, deserializedPerson.type)
        assertEquals(person.role, deserializedPerson.role)
        assertTrue(deserializedPerson.userData?.isFavorite == true)
    }
    
    @Test
    fun testJellyfinExportDataSerialization() {
        val exportData = JellyfinExportData(
            exportDate = "2024-01-01T12:00:00",
            serverUrl = "http://localhost:8096",
            userId = "user-id",
            favoriteMovies = listOf(
                JellyfinMovie(
                    id = "movie-1",
                    name = "Movie 1",
                    userData = UserData(isFavorite = true)
                )
            ),
            favoritePeople = listOf(
                JellyfinPerson(
                    id = "person-1",
                    name = "Actor 1",
                    userData = UserData(isFavorite = true)
                )
            )
        )
        
        val jsonString = json.encodeToString(exportData)
        val deserializedData = json.decodeFromString<JellyfinExportData>(jsonString)
        
        assertEquals(exportData.exportDate, deserializedData.exportDate)
        assertEquals(exportData.serverUrl, deserializedData.serverUrl)
        assertEquals(exportData.userId, deserializedData.userId)
        assertEquals(exportData.favoriteMovies.size, deserializedData.favoriteMovies.size)
        assertEquals(exportData.favoritePeople.size, deserializedData.favoritePeople.size)
        assertEquals(exportData.favoriteMovies[0].name, deserializedData.favoriteMovies[0].name)
        assertEquals(exportData.favoritePeople[0].name, deserializedData.favoritePeople[0].name)
    }
    
    @Test
    fun testImportResultCreation() {
        val result = ImportResult(
            totalMovies = 10,
            importedMovies = 8,
            failedMovies = 2,
            totalPeople = 5,
            importedPeople = 4,
            failedPeople = 1,
            errors = listOf("Error 1", "Error 2")
        )
        
        assertEquals(10, result.totalMovies)
        assertEquals(8, result.importedMovies)
        assertEquals(2, result.failedMovies)
        assertEquals(5, result.totalPeople)
        assertEquals(4, result.importedPeople)
        assertEquals(1, result.failedPeople)
        assertEquals(2, result.errors.size)
    }
}
