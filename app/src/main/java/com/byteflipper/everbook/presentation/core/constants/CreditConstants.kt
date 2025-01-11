package com.byteflipper.everbook.presentation.core.constants

import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.about.Credit
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.ui.UIText.StringResource

fun Constants.provideCredits() = listOf(
    Credit(
        name = "Acclorite/book-story",
        source = "This app is based on a fork of the Acclorite/book-story project. The original project is available under the GPL-3.0 license.",
        credits = listOf(),
        website = "https://github.com/Acclorite/book-story"
    ),
    Credit(
        name = "Tachiyomi (Mihon)",
        source = "GitHub",
        credits = listOf(
            UIText.StringResource(R.string.credits_design),
            UIText.StringResource(R.string.credits_ideas),
            UIText.StringValue("Readme")
        ),
        website = "https://www.github.com/mihonapp/mihon"
    ),
    Credit(
        name = "Kitsune",
        source = "GitHub",
        credits = listOf(
            UIText.StringResource(R.string.credits_updates),
            UIText.StringResource(R.string.credits_ideas)
        ),
        website = "https://www.github.com/Drumber/Kitsune"
    ),
    Credit(
        name = "Voyager",
        source = "Voyager website",
        credits = listOf(
            UIText.StringResource(R.string.credits_ideas)
        ),
        website = "https://voyager.adriel.cafe/"
    ),
    Credit(
        name = "Material Design Icons",
        source = "Google Fonts",
        credits = listOf(
            UIText.StringResource(R.string.credits_icon)
        ),
        website = "https://fonts.google.com/icons"
    ),
    Credit(
        name = "Material Design Fonts",
        source = "Google Fonts",
        credits = listOf(
            UIText.StringResource(R.string.credits_fonts)
        ),
        website = "https://fonts.google.com"
    ),
    Credit(
        name = "Weblate",
        source = "Hosted Weblate",
        credits = listOf(
            UIText.StringResource(R.string.credits_translation),
            UIText.StringResource(R.string.credits_contribution)
        ),
        website = "https://hosted.weblate.org/projects/book-story"
    ),
)