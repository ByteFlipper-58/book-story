package com.byteflipper.everbook.presentation.book_info

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.top_bar.TopAppBar
import com.byteflipper.everbook.presentation.core.components.top_bar.TopAppBarData
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.presentation.navigator.NavigatorIconButton
import com.byteflipper.everbook.ui.book_info.BookInfoEvent
import com.byteflipper.everbook.ui.theme.DefaultTransition
import com.byteflipper.everbook.ui.theme.Transitions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfoTopBar(
    book: Book,
    listState: LazyListState,
    editTitle: Boolean,
    titleValue: String,
    editAuthor: Boolean,
    authorValue: String,
    editDescription: Boolean,
    descriptionValue: String,
    editTitleMode: (BookInfoEvent.OnEditTitleMode) -> Unit,
    editAuthorMode: (BookInfoEvent.OnEditAuthorMode) -> Unit,
    editDescriptionMode: (BookInfoEvent.OnEditDescriptionMode) -> Unit,
    updateData: (BookInfoEvent.OnUpdateData) -> Unit,
    showMoreBottomSheet: (BookInfoEvent.OnShowMoreBottomSheet) -> Unit,
    navigateBack: () -> Unit
) {
    val firstVisibleItemIndex = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex
        }
    }

    val titleChanged = titleValue.isNotBlank()
            && titleValue.trim() != book.title.trim()
    val authorChanged = authorValue.isNotBlank()
            && authorValue.trim() != book.author.getAsString()?.trim()
    val descriptionChanged = descriptionValue.trim() != (book.description?.trim() ?: "")

    TopAppBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(0f),
        scrollBehavior = null,
        isTopBarScrolled = listState.canScrollBackward,

        shownTopBar = 0,
        topBars = listOf(
            TopAppBarData(
                contentID = 0,
                contentNavigationIcon = {
                    if (
                        editTitle ||
                        editAuthor ||
                        editDescription
                    ) {
                        IconButton(
                            icon = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = R.string.exit_editing_content_desc,
                            disableOnClick = true
                        ) {
                            if (editTitle) {
                                editTitleMode(BookInfoEvent.OnEditTitleMode(false))
                            }

                            if (editAuthor) {
                                editAuthorMode(BookInfoEvent.OnEditAuthorMode(false))
                            }

                            if (editDescription) {
                                editDescriptionMode(BookInfoEvent.OnEditDescriptionMode(false))
                            }
                        }
                    } else {
                        NavigatorBackIconButton {
                            navigateBack()
                        }
                    }
                },
                contentTitle = {
                    DefaultTransition(firstVisibleItemIndex.value > 0 && !editTitle) {
                        Text(
                            book.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                },
                contentActions = {
                    Box {
                        DefaultTransition(
                            visible = !editTitle &&
                                    !editAuthor &&
                                    !editDescription
                        ) {
                            NavigatorIconButton {
                                showMoreBottomSheet(BookInfoEvent.OnShowMoreBottomSheet)
                            }
                        }
                        AnimatedVisibility(
                            visible = editTitle ||
                                    editAuthor ||
                                    editDescription,
                            enter = Transitions.DefaultTransitionIn,
                            exit = fadeOut(tween(200))
                        ) {
                            IconButton(
                                icon = Icons.Outlined.Done,
                                contentDescription = R.string.apply_changes_content_desc,
                                disableOnClick = true,
                                enabled = titleChanged ||
                                        authorChanged ||
                                        descriptionChanged,
                                color = if (
                                    titleChanged ||
                                    authorChanged ||
                                    descriptionChanged
                                ) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                updateData(BookInfoEvent.OnUpdateData)
                            }
                        }
                    }
                }
            )
        )
    )
}