package com.byteflipper.everbook.domain.use_case.permission

import androidx.activity.ComponentActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.domain.repository.PermissionRepository
import javax.inject.Inject

class GrantStoragePermission @Inject constructor(
    private val repository: PermissionRepository
) {

    @OptIn(ExperimentalPermissionsApi::class)
    suspend fun execute(
        activity: ComponentActivity,
        storagePermissionState: PermissionState
    ): Boolean {
        return repository.grantStoragePermission(
            activity = activity,
            storagePermissionState = storagePermissionState
        )
    }
}