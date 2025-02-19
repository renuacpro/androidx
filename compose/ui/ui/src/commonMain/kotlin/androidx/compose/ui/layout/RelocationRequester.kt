/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.layout

import androidx.compose.runtime.collection.MutableVector
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.unit.toSize

/**
 * This class can be used to send relocation requests. Pass it as a parameter to
 * [Modifier.relocationRequester()][relocationRequester].
 *
 * For instance, you can call [RelocationRequester.bringIntoView][bringIntoView] to
 * make all the scrollable parents scroll so that the specified item is brought into parent
 * bounds. This sample demonstrates this use case:
 *
 * Here is a sample where a composable is brought into view:
 * @sample androidx.compose.ui.samples.BringIntoViewSample
 *
 * Here is a sample where a part of a composable is brought into view:
 * @sample androidx.compose.ui.samples.BringPartOfComposableIntoViewSample
 */
@ExperimentalComposeUiApi
class RelocationRequester {
    internal val modifiers: MutableVector<RelocationRequesterModifier> = mutableVectorOf()

    /**
     * Bring this item into bounds by making all the scrollable parents scroll appropriately.
     *
     * @param rect The rectangle (In local coordinates) that should be brought into view. If you
     * don't specify the coordinates, the coordinates of the
     * [Modifier.relocationRequester()][relocationRequester] associated with this
     * [RelocationRequester] will be used.
     *
     * Here is a sample where a composable is brought into view:
     * @sample androidx.compose.ui.samples.BringIntoViewSample
     *
     * Here is a sample where a part of a composable is brought into view:
     * @sample androidx.compose.ui.samples.BringPartOfComposableIntoViewSample
     */
    suspend fun bringIntoView(rect: Rect? = null) {
        modifiers.forEach {
            it.bringRectIntoView(rect ?: it.relocationRequesterNode.size.toSize().toRect())
        }
    }
}
