/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.activity.ComponentActivity;
import com.google.accompanist.permissions.ExperimentalPermissionsApi;
import com.google.accompanist.permissions.PermissionState;
import com.google.accompanist.permissions.isGranted;
import com.google.accompanist.permissions.shouldShowRationale;
import kotlinx.coroutines.delay;
import kotlinx.coroutines.yield;
import com.byteflipper.everbook.domain.repository.PermissionRepository;
import com.byteflipper.everbook.presentation.core.util.launchActivity;
import javax.inject.Inject;
import javax.inject.Singleton;

private const val GRANT_URI = "GRANT URI PERM, REPO";
private const val RELEASE_URI = "RELEASE URI PERM, REPO";
private const val NOTIFICATIONS_PERMISSION = "NOTIFIC PERM, REPO";

@Singleton
class PermissionRepositoryImpl @Inject constructor(
    private val application: Application
) : PermissionRepository {

    override suspend fun grantPersistableUriPermission(uri: Uri) {
        Log.i(GRANT_URI, "Granting persistable uri permission to \"${uri.path}\" URI.");

        try {
            application.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
        } catch (e: Exception) {
            e.printStackTrace();
            Log.e(GRANT_URI, "Could not grant URI permission.");
        }
    }

    override suspend fun releasePersistableUriPermission(uri: Uri) {
        Log.i(RELEASE_URI, "Releasing persistable uri permission from \"${uri.path}\" URI.");

        try {
            application.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
        } catch (e: Exception) {
            e.printStackTrace();
            Log.w(RELEASE_URI, "No granted URI permission found.");
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override suspend fun grantNotificationsPermission(
        activity: ComponentActivity,
        notificationsPermissionState: PermissionState
    ): Boolean {
        Log.i(NOTIFICATIONS_PERMISSION, "Requested notifications permission");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.w(NOTIFICATIONS_PERMISSION, "Granted: API is below 33");
            return true;
        }

        if (notificationsPermissionState.status.isGranted) {
            Log.i(NOTIFICATIONS_PERMISSION, "Granted: Notifications Permission is already granted");
            return true;
        }

        if (!notificationsPermissionState.status.shouldShowRationale) {
            notificationsPermissionState.launchPermissionRequest();
        } else {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName);

            intent.launchActivity(activity) {
                Log.e(
                    NOTIFICATIONS_PERMISSION,
                    "Could not launch \"APP_NOTIFICATION_SETTINGS\" activity"
                );
                return false;
            };
        }

        for (i in 1..20) {
            if (!notificationsPermissionState.status.isGranted) {
                delay(1000);
                yield();
                continue;
            }

            yield();

            Log.i(NOTIFICATIONS_PERMISSION, "Successfully granted");
            return true;
            break;
        }

        Log.e(NOTIFICATIONS_PERMISSION, "Not granted: Timeout");
        return false;
    }
}
