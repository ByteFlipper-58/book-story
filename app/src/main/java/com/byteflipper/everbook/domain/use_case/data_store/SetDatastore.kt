package com.byteflipper.everbook.domain.use_case.data_store

import androidx.datastore.preferences.core.Preferences
import com.byteflipper.everbook.domain.repository.DataStoreRepository
import javax.inject.Inject

class SetDatastore @Inject constructor(
    private val repository: DataStoreRepository
) {

    suspend fun <T> execute(key: Preferences.Key<T>, value: T) {
        repository.putDataToDataStore(key, value)
    }
}