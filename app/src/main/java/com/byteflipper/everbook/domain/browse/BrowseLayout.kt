package com.byteflipper.everbook.domain.browse

enum class BrowseLayout {
    LIST, GRID
}

fun String.toBrowseLayout(): BrowseLayout {
    return BrowseLayout.valueOf(this)
}