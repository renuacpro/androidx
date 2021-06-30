/*
 * Copyright 2020 The Android Open Source Project
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
package androidx.compose.ui.graphics

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import org.jetbrains.skiko.skia.native.SkRect
import org.jetbrains.skiko.skia.native.SkRRect
import org.jetbrains.skiko.skia.native.RRect
import org.jetbrains.skiko.skia.native.SkVector
import kotlinx.cinterop.*

fun makeLTRB(l: Float, t: Float, r: Float, b: Float): CPointer<SkRect> {
    val rect = nativeHeap.alloc<SkRect>()
    println("TODO: make Rect instead of SkRect otherwise we are leaking")

    if (l > r) error("makeLTRB expected l <= r, got $l > $r")
    if (t > b) error("makeLTRB expected t <= b, got $t > $b")

    rect.fLeft = l
    rect.fRight = r
    rect.fTop = t
    rect.fBottom = b

    return rect.ptr
}

fun makeXYWH(x: Float, y: Float, w: Float, h: Float): CPointer<SkRect> =
    makeLTRB(x, y, x + w, y + h)

fun Rect.toSkiaNativeRect(): CPointer<SkRect> = makeLTRB(left, top, right, bottom)

/*
fun org.jetbrains.skija.Rect.toComposeRect() =
    androidx.compose.ui.geometry.Rect(left, top, right, bottom)
*/

fun SkVector(x: Float, y: Float): CPointer<SkVector> {
    val skVector = nativeHeap.alloc<SkVector>()
    println("TODO: leaking SkVector")
    skVector.set(x, y)
    return skVector.ptr
}

fun makeComplexLTRB(l: Float, t: Float, r: Float, b: Float, radii: FloatArray): RRect {
    val rrect = RRect()
    /*
    memScoped {
        val radiiArray = allocArrayOf(
            radii[0], radii[1],
            radii[2], radii[3],
            radii[4], radii[5],
            radii[6], radii[7]
        )

        rrect.setRectRadii(
                makeLTRB(l, t, r, b),
                radiiArray.reinterpret<SkVector>()
        )
    }
    */
    println("TODO: teach makeComplexLTRB to pass radii to RRect: ${radii.map { it.toString() }}. Taking the first two.")
    // TODO: just take top level x and y radii for now.
    rrect.setRectXY(makeLTRB(l, t, r, b), radii[0], radii[1])

    return rrect
}

fun makeComplexLTRB(l: Float, t: Float, r: Float, b: Float, radiusX: Float, radiusY: Float): RRect {
    val rrect = RRect()
    rrect.setRectXY(makeLTRB(l, t, r, b), radiusX, radiusY)
    return rrect
}


fun RoundRect.toSkiaNativeRRect(): RRect {
    val radii = FloatArray(8)

    radii[0] = topLeftCornerRadius.x
    radii[1] = topLeftCornerRadius.y

    radii[2] = topRightCornerRadius.x
    radii[3] = topRightCornerRadius.y

    radii[4] = bottomRightCornerRadius.x
    radii[5] = bottomRightCornerRadius.y

    radii[6] = bottomLeftCornerRadius.x
    radii[7] = bottomLeftCornerRadius.y

    return makeComplexLTRB(left, top, right, bottom, radii)
}

