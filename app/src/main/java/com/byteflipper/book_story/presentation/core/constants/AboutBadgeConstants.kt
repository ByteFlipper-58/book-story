package com.byteflipper.book_story.presentation.core.constants

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import com.byteflipper.book_story.R
import com.byteflipper.book_story.domain.model.Badge

fun Constants.provideAboutBadges() = listOf(
    Badge(
        id = "x",
        drawable = R.drawable.x_logo,
        imageVector = null,
        contentDescription = R.string.x_content_desc,
        url = "https://www.x.com/byteflipper"
    ),
    Badge(
        id = "vk",
        drawable = R.drawable.vk_24,
        imageVector = null,
        contentDescription = R.string.reddit_content_desc,
        url = "https://vk.com/byteflipper/"
    ),
    Badge(
        id = "tryzub",
        drawable = R.drawable.tryzub,
        imageVector = null,
        contentDescription = R.string.tryzub_content_desc,
        url = null
    ),
    Badge(
        id = "telegram",
        drawable = R.drawable.telegram_24,
        imageVector = null,
        contentDescription = R.string.patreon_content_desc,
        url = "https://t.me/ByteFlipper"
    ),
    Badge(
        id = "github_profile",
        drawable = null,
        imageVector = Icons.Default.Person,
        contentDescription = R.string.github_profile_content_desc,
        url = "https://www.github.com/ByteFlipper-58"
    ),
)