package com.byteflipper.everbook.domain.library.category

import androidx.compose.runtime.Immutable

@Immutable
enum class Category {
    READING,
    ALREADY_READ,
    PLANNING,
    DROPPED
}