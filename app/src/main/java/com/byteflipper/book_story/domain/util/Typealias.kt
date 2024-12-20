package com.byteflipper.book_story.domain.util

import android.graphics.Bitmap
import com.byteflipper.book_story.presentation.core.navigation.Navigator

typealias CoverImage = Bitmap
typealias Selected = Boolean
typealias ID = Int
typealias Route = String
typealias OnNavigate = (Navigator.() -> Unit) -> Unit
