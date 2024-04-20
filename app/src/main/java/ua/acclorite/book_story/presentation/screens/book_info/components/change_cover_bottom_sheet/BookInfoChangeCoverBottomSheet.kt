package ua.acclorite.book_story.presentation.screens.book_info.components.change_cover_bottom_sheet

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.acclorite.book_story.R
import ua.acclorite.book_story.presentation.screens.book_info.data.BookInfoEvent
import ua.acclorite.book_story.presentation.screens.book_info.data.BookInfoState
import ua.acclorite.book_story.presentation.screens.history.data.HistoryEvent
import ua.acclorite.book_story.presentation.screens.library.data.LibraryEvent

/**
 * Change cover bottom sheet. Lets user select photo from the gallery and replaces old one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfoChangeCoverBottomSheet(
    state: State<BookInfoState>,
    onEvent: (BookInfoEvent) -> Unit,
    onLibraryUpdateEvent: (LibraryEvent.OnUpdateBook) -> Unit,
    onHistoryUpdateEvent: (HistoryEvent.OnUpdateBook) -> Unit
) {
    val context = LocalContext.current
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onEvent(
                    BookInfoEvent.OnChangeCover(
                        uri,
                        context,
                        refreshList = {
                            onLibraryUpdateEvent(LibraryEvent.OnUpdateBook(it))
                            onHistoryUpdateEvent(HistoryEvent.OnUpdateBook(it))
                        }
                    )
                )
                Toast.makeText(
                    context,
                    context.getString(R.string.cover_image_changed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = {
            onEvent(BookInfoEvent.OnShowHideChangeCoverBottomSheet)
        },
        sheetState = rememberModalBottomSheetState(true),
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {

        BookInfoChangeCoverBottomSheetItem(
            icon = Icons.Default.ImageSearch,
            text = stringResource(id = R.string.change_cover),
            description = stringResource(id = R.string.change_cover_desc)
        ) {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        if (state.value.book.coverImage != null) {
            BookInfoChangeCoverBottomSheetItem(
                icon = Icons.Default.HideImage,
                text = stringResource(id = R.string.delete_cover),
                description = stringResource(id = R.string.delete_cover_desc)
            ) {
                onEvent(
                    BookInfoEvent.OnDeleteCover(
                        refreshList = {
                            onLibraryUpdateEvent(LibraryEvent.OnUpdateBook(it))
                            onHistoryUpdateEvent(HistoryEvent.OnUpdateBook(it))
                        }
                    )
                )
                Toast.makeText(
                    context,
                    context.getString(R.string.cover_image_deleted),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        Spacer(
            modifier = Modifier.height(
                8.dp + navigationBarPadding
            )
        )
    }
}