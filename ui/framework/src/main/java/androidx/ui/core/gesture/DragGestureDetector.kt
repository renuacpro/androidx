/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.ui.core.gesture

import androidx.ui.core.DensityAmbient
import androidx.ui.core.Direction
import androidx.ui.core.PointerEventPass
import androidx.ui.core.PointerInput
import androidx.ui.core.PointerInputChange
import androidx.ui.core.PxPosition
import androidx.ui.core.changedToDown
import androidx.ui.core.changedToDownIgnoreConsumed
import androidx.ui.core.changedToUp
import androidx.ui.core.changedToUpIgnoreConsumed
import androidx.ui.core.consumeDownChange
import androidx.ui.core.consumePositionChange
import androidx.ui.core.gesture.util.VelocityTracker
import androidx.ui.core.ipx
import androidx.ui.core.positionChange
import androidx.ui.core.px
import androidx.ui.core.withDensity
import com.google.r4a.Children
import com.google.r4a.Component
import com.google.r4a.composer

open class DragObserver {
    open fun onStart() {}
    open fun onDrag(dragDistance: PxPosition) = dragDistance
    open fun onStop(velocity: PxPosition) {}
}

// TODO(shepshapard): Convert to functional component with effects once effects are ready.
/**
 *
 */
class DragGestureDetector(
    @Children var children: () -> Unit
) : Component() {

    private val recognizer = DragGestureRecognizer()

    var canDrag: ((Direction) -> Boolean)?
        get() = recognizer.canDrag
        set(value) {
            recognizer.canDrag = value
        }
    // TODO(b/129784010): Consider also allowing onStart, onDrag, and onEnd to be set individually.
    var dragObserver
        get() = recognizer.dragObserver
        set(value) {
            recognizer.dragObserver = value
        }

    override fun compose() {
        <DensityAmbient.Consumer> density ->
            withDensity(density) {
                recognizer.touchSlop = TouchSlop.toIntPx()
                <PointerInput pointerInputHandler=recognizer.pointerInputHandler>
                    <children />
                </PointerInput>
            }
        </DensityAmbient.Consumer>
    }
}

internal class DragGestureRecognizer {
    private val pointerTrackers: MutableMap<Int, PointerTrackingData> = mutableMapOf()
    private var passedSlop = false
    private var pointerCount = 0
    var touchSlop = 0.ipx

    var canDrag: ((Direction) -> Boolean)? = null
    var dragObserver: DragObserver? = null

    val pointerInputHandler = { pointerInputChange: PointerInputChange, pass: PointerEventPass ->
        var change: PointerInputChange = pointerInputChange

        if (pass == PointerEventPass.InitialDown && change.changedToDownIgnoreConsumed()) {
            pointerCount++
        }

        if (pass == PointerEventPass.InitialDown && change.changedToDown() && passedSlop) {
            // If we are passedSlop, we are actively dragging so we want to prevent any children
            // from reacting to any down change.
            change = change.consumeDownChange()
        }

        if (pass == PointerEventPass.PostUp) {
            if (change.changedToUpIgnoreConsumed()) {
                // This pointer is up (consumed or not), so we should stop tracking information
                // about it.  Get a reference for the velocity tracker in case this is the last
                // pointer and thus we are going to fling.
                val velocityTracker = pointerTrackers.remove(change.id)?.velocityTracker
                if (pointerCount == 1) {
                    if (passedSlop && change.changedToUp()) {
                        // There is one pointer, we have passed slop, and there was an unconsumed
                        // up event, so we should fire the onStop with the velocity tracked
                        // for the last pointer.

                        // TODO(shepshapard): handle the case that the velocity tracker for the
                        // given pointer is null, by throwing an exception or printing a warning,
                        // or something else.
                        val velocity =
                            velocityTracker?.calculateVelocity()?.pixelsPerSecond
                                ?: PxPosition.Origin
                        velocityTracker?.resetTracking()
                        dragObserver?.onStop(PxPosition(velocity.x, velocity.y))

                        // We responded to the up change, so consume it.
                        change = change.consumeDownChange()
                    }
                    // The last pointer is up whether or not up was consumed, so we should reset
                    // that we passed slop.
                    passedSlop = false
                }
            } else if (change.changedToDownIgnoreConsumed()) {
                // If a pointer has changed to down, we should start tracking information about it.
                pointerTrackers[change.id] = PointerTrackingData()
                    .apply {
                        velocityTracker.addPosition(
                            change.current.timestamp!!,
                            PxPosition(
                                change.current.position!!.dx.px,
                                change.current.position!!.dy.px
                            )
                        )
                    }
            } else if (change.current.down) {
                // TODO(shepshapard): handle the case that the pointerTrackingData is null, either
                // with an exception or a logged error, or something else.
                val pointerTracker: PointerTrackingData? = pointerTrackers[change.id]

                if (pointerTracker != null) {
                    // If the pointer is currently down, we should track its velocity.
                    pointerTracker.velocityTracker.addPosition(
                        change.current.timestamp!!,
                        PxPosition(
                            change.current.position!!.dx.px,
                            change.current.position!!.dy.px
                        )
                    )

                    val dx = change.positionChange().dx
                    val dy = change.positionChange().dy

                    // If we aren't passed slop, calculate things related to slop, and start drag
                    // if we do pass touch slop.
                    if (!passedSlop) {
                        // TODO(shepshapard): I believe the logic in this block could be simplified
                        // to be much more clear.  Will need to revisit. The need to make
                        // improvements may be rendered obsolete with upcoming changes however.

                        val directionX = when {
                            dx == 0f -> null
                            dx < 0f -> Direction.LEFT
                            else -> Direction.RIGHT
                        }
                        val directionY = when {
                            dy == 0f -> null
                            dy < 0f -> Direction.UP
                            else -> Direction.DOWN
                        }

                        val canDragX =
                            if (directionX != null) {
                                canDrag?.invoke(directionX) ?: true
                            } else false
                        val canDragY =
                            if (directionY != null) {
                                canDrag?.invoke(directionY) ?: true
                            } else false

                        pointerTracker.dxUnderSlop += dx
                        pointerTracker.dyUnderSlop += dy

                        val passedSlopX =
                            canDragX && Math.abs(pointerTracker.dxUnderSlop) > touchSlop.value
                        val passedSlopY =
                            canDragY && Math.abs(pointerTracker.dyUnderSlop) > touchSlop.value

                        if (passedSlopX || passedSlopY) {
                            passedSlop = true
                            dragObserver?.onStart()
                        } else {
                            if (!canDragX &&
                                ((directionX == Direction.LEFT && pointerTracker.dxUnderSlop < 0) ||
                                        (directionX == Direction.RIGHT &&
                                                pointerTracker.dxUnderSlop > 0))
                            ) {
                                pointerTracker.dxUnderSlop = 0f
                            }
                            if (!canDragY &&
                                ((directionY == Direction.LEFT && pointerTracker.dyUnderSlop < 0) ||
                                        (directionY == Direction.DOWN &&
                                                pointerTracker.dyUnderSlop > 0))
                            ) {
                                pointerTracker.dyUnderSlop = 0f
                            }
                        }
                    }

                    // At this point, check to see if we have passed touch slop, and if we have
                    // go ahead and drag and consume.
                    if (passedSlop) {
                        change = dragObserver?.run {
                            val (consumedDx, consumedDy) = onDrag(
                                PxPosition(
                                    dx.px,
                                    dy.px
                                )
                            )
                            pointerInputChange.consumePositionChange(
                                consumedDx.value,
                                consumedDy.value
                            )
                        } ?: pointerInputChange
                    }
                }
            }
        }

        if (pass == PointerEventPass.PostDown && change.changedToUpIgnoreConsumed()) {
            pointerCount--
        }

        change
    }

    internal data class PointerTrackingData(
        val velocityTracker: VelocityTracker = VelocityTracker(),
        var dxUnderSlop: Float = 0f,
        var dyUnderSlop: Float = 0f
    )
}