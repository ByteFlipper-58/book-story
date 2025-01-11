package com.byteflipper.everbook.domain.use_case.data_store

import com.byteflipper.everbook.domain.repository.DataStoreRepository
import com.byteflipper.everbook.ui.main.MainState
import javax.inject.Inject

class GetAllSettings @Inject constructor(
    private val repository: DataStoreRepository
) {

    suspend fun execute(): MainState {
        return repository.getAllSettings()
    }
}