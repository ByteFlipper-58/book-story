/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.di

import android.app.Application
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.commonmark.node.BlockQuote
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.byteflipper.everbook.data.local.room.BookDao
import com.byteflipper.everbook.data.local.room.BookDatabase
import com.byteflipper.everbook.data.local.room.DatabaseHelper
import com.byteflipper.everbook.data.remote.GithubAPI
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideGithubApi(): GithubAPI {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(GithubAPI.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GithubAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideCommonmarkParser(): Parser {
        return Parser
            .builder()
            .enabledBlockTypes(
                setOf(
                    Heading::class.java,
                    HtmlBlock::class.java,
                    ThematicBreak::class.java,
                    BlockQuote::class.java,
                    FencedCodeBlock::class.java,
                    IndentedCodeBlock::class.java,
                    ThematicBreak::class.java
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(app: Application): BookDao {
        // Additional Migrations
        DatabaseHelper.MIGRATION_7_8.removeBooksDir(app)

        return Room.databaseBuilder(
            app,
            BookDatabase::class.java,
            "book_db"
        )
            .addMigrations(
                DatabaseHelper.MIGRATION_2_3, // creates LanguageHistoryEntity table(if does not exist)
                DatabaseHelper.MIGRATION_4_5, // creates ColorPresetEntity table(if does not exist)
                DatabaseHelper.MIGRATION_5_6, // creates FavoriteDirectoryEntity table(if does not exist)
            )
            .allowMainThreadQueries()
            .build()
            .dao
    }
}