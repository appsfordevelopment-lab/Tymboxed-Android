package dev.ambitionsoftware.tymeboxed.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsCoordinator

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PermissionsEntryPoint {
    fun permissionsCoordinator(): PermissionsCoordinator
}
