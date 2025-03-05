package com.byteflipper.everbook.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.byteflipper.everbook.data.local.data_store.DataStore
import com.byteflipper.everbook.data.local.data_store.DataStoreImpl
import com.byteflipper.everbook.data.local.notification.UpdatesNotificationService
import com.byteflipper.everbook.data.local.notification.UpdatesNotificationServiceImpl
import com.byteflipper.everbook.data.mapper.book.BookMapper
import com.byteflipper.everbook.data.mapper.book.BookMapperImpl
import com.byteflipper.everbook.data.mapper.color_preset.ColorPresetMapper
import com.byteflipper.everbook.data.mapper.color_preset.ColorPresetMapperImpl
import com.byteflipper.everbook.data.mapper.history.HistoryMapper
import com.byteflipper.everbook.data.mapper.history.HistoryMapperImpl
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.data.parser.FileParserImpl
import com.byteflipper.everbook.data.parser.TextParser
import com.byteflipper.everbook.data.parser.TextParserImpl
import com.byteflipper.everbook.data.repository.BookRepositoryImpl
import com.byteflipper.everbook.data.repository.ColorPresetRepositoryImpl
import com.byteflipper.everbook.data.repository.DataStoreRepositoryImpl
import com.byteflipper.everbook.data.repository.FileSystemRepositoryImpl
import com.byteflipper.everbook.data.repository.HistoryRepositoryImpl
import com.byteflipper.everbook.data.repository.PermissionRepositoryImpl
import com.byteflipper.everbook.data.repository.RemoteRepositoryImpl
import com.byteflipper.everbook.domain.repository.BookRepository
import com.byteflipper.everbook.domain.repository.ColorPresetRepository
import com.byteflipper.everbook.domain.repository.DataStoreRepository
import com.byteflipper.everbook.domain.repository.FileSystemRepository
import com.byteflipper.everbook.domain.repository.HistoryRepository
import com.byteflipper.everbook.domain.repository.PermissionRepository
import com.byteflipper.everbook.domain.repository.RemoteRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDataStore(
        dataStoreImpl: DataStoreImpl
    ): DataStore

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepositoryImpl: BookRepositoryImpl
    ): BookRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindColorPresetRepository(
        colorPresetRepositoryImpl: ColorPresetRepositoryImpl
    ): ColorPresetRepository

    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(
        dataStoreRepositoryImpl: DataStoreRepositoryImpl
    ): DataStoreRepository

    @Binds
    @Singleton
    abstract fun bindFileSystemRepository(
        fileSystemRepositoryImpl: FileSystemRepositoryImpl
    ): FileSystemRepository

    @Binds
    @Singleton
    abstract fun bindRemoteRepository(
        remoteRepositoryImpl: RemoteRepositoryImpl
    ): RemoteRepository

    @Binds
    @Singleton
    abstract fun bindPermissionRepository(
        permissionRepositoryImpl: PermissionRepositoryImpl
    ): PermissionRepository

    @Binds
    @Singleton
    abstract fun bindBookMapper(
        bookMapperImpl: BookMapperImpl
    ): BookMapper

    @Binds
    @Singleton
    abstract fun bindHistoryMapper(
        historyMapperImpl: HistoryMapperImpl
    ): HistoryMapper

    @Binds
    @Singleton
    abstract fun bindColorPresetMapper(
        colorPresetMapperImpl: ColorPresetMapperImpl
    ): ColorPresetMapper

    @Binds
    @Singleton
    abstract fun bindFileParser(
        fileParserImpl: FileParserImpl
    ): FileParser

    @Binds
    @Singleton
    abstract fun bindTextParser(
        textParserImpl: TextParserImpl
    ): TextParser

    @Binds
    @Singleton
    abstract fun bindNotificationService(
        updatesNotificationServiceImpl: UpdatesNotificationServiceImpl
    ): UpdatesNotificationService
}