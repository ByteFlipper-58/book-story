package com.byteflipper.book_story.domain.use_case.favorite_directory

import com.byteflipper.book_story.domain.repository.FavoriteDirectoryRepository
import javax.inject.Inject

class UpdateFavoriteDirectory @Inject constructor(
    private val repository: FavoriteDirectoryRepository
) {

    suspend fun execute(path: String) {
        return repository.updateFavoriteDirectory(path)
    }
}