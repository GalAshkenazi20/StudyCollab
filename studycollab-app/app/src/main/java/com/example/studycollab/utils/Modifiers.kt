package com.example.studycollab.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Extension to allow mouse wheel scrolling on any scrollable state.
 */
@Composable
fun Modifier.mouseWheelScroll(state: ScrollableState): Modifier {
    val scope = rememberCoroutineScope()
    return this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) {
                    val delta = event.changes.first().scrollDelta.y
                    scope.launch {
                        state.dispatchRawDelta(delta * 50f) // Adjust 50f for sensitivity
                    }
                }
            }
        }
    }
}