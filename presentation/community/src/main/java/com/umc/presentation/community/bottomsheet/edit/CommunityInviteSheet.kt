package com.umc.presentation.community.bottomsheet.edit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import android.view.WindowManager
import com.umc.component.theme.AppStrings
import com.umc.component.theme.black
import com.umc.component.theme.grey000
import com.umc.component.theme.grey600
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Only the handle owns the sheet's drag gesture; content scroll has no parent sheet to move. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityInviteSheet(
    height: Dp,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val heightPx = with(density) { height.toPx() }
    val distanceThreshold = with(density) { 56.dp.toPx() }
    val velocityThreshold = with(density) { 125.dp.toPx() }
    val offset = remember { Animatable(heightPx) }
    var measuredHeight by remember { mutableIntStateOf(heightPx.roundToInt()) }
    var closing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val dismissSheet: () -> Unit = {
        if (!closing) {
            closing = true
            keyboard?.hide()
            scope.launch {
                offset.animateTo(measuredHeight.toFloat(), tween(200))
                currentOnDismiss()
            }
        }
    }
    LaunchedEffect(Unit) { offset.animateTo(0f, tween(250)) }

    Dialog(
        onDismissRequest = dismissSheet,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        // The full-window scrim below owns dimming and outside taps.
        SideEffect {
            window?.setDimAmount(0f)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize().background(black().copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = dismissSheet,
                    ),
            )
            Box(Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .offset { IntOffset(0, offset.value.roundToInt()) }
                        .onSizeChanged { measuredHeight = it.height }
                        .semantics {
                            paneTitle = AppStrings.CHAT_MENU_INVITE_PARTICIPANTS
                            dismiss { dismissSheet(); true }
                        },
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = grey000(),
                ) {
                    Column(Modifier.navigationBarsPadding().height(height)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                .draggable(
                                    state = rememberDraggableState { delta ->
                                        if (!closing) scope.launch {
                                            offset.snapTo((offset.value + delta).coerceIn(0f, measuredHeight.toFloat()))
                                        }
                                    },
                                    orientation = Orientation.Vertical,
                                    enabled = !closing,
                                    startDragImmediately = offset.isRunning,
                                    onDragStarted = { offset.stop() },
                                    onDragStopped = { velocity ->
                                        if (offset.value >= distanceThreshold ||
                                            (offset.value > 0f && velocity >= velocityThreshold)
                                        ) dismissSheet()
                                        else offset.animateTo(0f, tween(200))
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            BottomSheetDefaults.DragHandle(color = grey600())
                        }
                        content()
                    }
                }
            }
        }
    }
}
