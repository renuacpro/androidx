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

package androidx.compose.ui.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SkiaNativeCanvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asNativePath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
// import androidx.compose.ui.graphics.toSkijaRect
import androidx.compose.ui.node.OwnedLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
/*
import org.jetbrains.skiko.skia.native.Picture
import org.jetbrains.skiko.skia.native.PictureRecorder
import org.jetbrains.skiko.skia.native.Point3
import org.jetbrains.skiko.skia.native.ShadowUtils
 */

/* internal */ class SkiaNativeLayer(
    private var density: Density,
    private val invalidateParentLayer: () -> Unit,
    private val drawBlock: (Canvas) -> Unit,
    private val onDestroy: () -> Unit = {}
) : OwnedLayer {
    private var size = IntSize.Zero
    private var position = IntOffset.Zero
    private var outlineCache =
        OutlineCache(density, size, RectangleShape, LayoutDirection.Ltr)
    private val matrix = Matrix()
    private val pictureRecorder: Any // = PictureRecorder()
        get() = TODO("implement native picture recorder")
    private var picture: Any? = null // Picture? = null
        get() = TODO("implement native picture")
    private var isDestroyed = false

    private var transformOrigin: TransformOrigin = TransformOrigin.Center
    private var translationX: Float = 0f
    private var translationY: Float = 0f
    private var rotationX: Float = 0f
    private var rotationY: Float = 0f
    private var rotationZ: Float = 0f
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f
    private var alpha: Float = 1f
    private var clip: Boolean = false
    private var shadowElevation: Float = 0f

    override val layerId = lastId++

    @ExperimentalComposeUiApi
    override val ownerViewId: Long
        get() = 0

    override fun destroy() {
        // picture?.close()
        // pictureRecorder.close()
        isDestroyed = true
        onDestroy()
    }

    override fun resize(size: IntSize) {
        if (size != this.size) {
            this.size = size
            outlineCache.size = size
            updateMatrix()
            invalidate()
        }
    }

    override fun mapOffset(point: Offset, inverse: Boolean): Offset {
        return getMatrix(inverse).map(point)
    }

    override fun mapBounds(rect: MutableRect, inverse: Boolean) {
        getMatrix(inverse).map(rect)
    }


    private fun getMatrix(inverse: Boolean): Matrix {
        return if (inverse) {
            Matrix().apply {
                setFrom(matrix)
                invert()
            }
        } else {
            matrix
        }
    }


    override fun move(position: IntOffset) {
        if (position != this.position) {
            this.position = position
            invalidateParentLayer()
        }
    }

    override fun updateLayerProperties(
        scaleX: Float,
        scaleY: Float,
        alpha: Float,
        translationX: Float,
        translationY: Float,
        shadowElevation: Float,
        rotationX: Float,
        rotationY: Float,
        rotationZ: Float,
        cameraDistance: Float,
        transformOrigin: TransformOrigin,
        shape: Shape,
        clip: Boolean,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        this.transformOrigin = transformOrigin
        this.translationX = translationX
        this.translationY = translationY
        this.rotationX = rotationX
        this.rotationY = rotationY
        this.rotationZ = rotationZ
        this.scaleX = scaleX
        this.scaleY = scaleY
        this.alpha = alpha
        this.clip = clip
        this.shadowElevation = shadowElevation
        this.density = density
        outlineCache.shape = shape
        outlineCache.layoutDirection = layoutDirection
        outlineCache.density = density
        updateMatrix()
        invalidate()
    }

    // TODO(demin): support perspective projection for rotationX/rotationY (as in Android)
    // TODO(njawad) Add camera distance leveraging Sk3DView along with rotationX/rotationY
    // see https://cs.android.com/search?q=RenderProperties.cpp&sq= updateMatrix method
    // for how 3d transformations along with camera distance are applied. b/173402019
    private fun updateMatrix() {
        val pivotX = transformOrigin.pivotFractionX * size.width
        val pivotY = transformOrigin.pivotFractionY * size.height

        matrix.reset()
        matrix *= Matrix().apply {
            translate(x = -pivotX, y = -pivotY)
        }
        matrix *= Matrix().apply {
            translate(translationX, translationY)
            rotateX(rotationX)
            rotateY(rotationY)
            rotateZ(rotationZ)
            scale(scaleX, scaleY)
        }
        matrix *= Matrix().apply {
            translate(x = pivotX, y = pivotY)
        }
    }

    override fun invalidate() {
        if (!isDestroyed && picture != null) {
            // picture?.close()
            picture = null
            invalidateParentLayer()
        }
    }

    override fun drawLayer(canvas: Canvas) {
        TODO("implement SkiaNativeLayer.drawLayer")
        /*
        outlineCache.density = getDensity()
        if (picture == null) {
            val bounds = size.toSize().toRect()
            val pictureCanvas = pictureRecorder.beginRecording(bounds.toSkijaRect())
            performDrawLayer(SkiaNativeCanvas(pictureCanvas), bounds)
            picture = pictureRecorder.finishRecordingAsPicture()
        }

        canvas.save()
        canvas.concat(matrix)
        canvas.translate(position.x.toFloat(), position.y.toFloat())
        canvas.nativeCanvas.drawPicture(picture!!, null, null)
        canvas.restore()
         */
    }

    private fun performDrawLayer(canvas: SkiaNativeCanvas, bounds: Rect) {
        TODO("implement performDrawLayer")
        /*
        if (alpha > 0) {
            if (shadowElevation > 0) {
                drawShadow(canvas)
            }

            if (alpha < 1) {
                canvas.saveLayer(
                    bounds,
                    Paint().apply { alpha = this@SkiaNativeLayer.alpha }
                )
            } else {
                canvas.save()
            }

            if (clip) {
                when (val outline = outlineCache.outline) {
                    is Outline.Rectangle -> canvas.clipRect(outline.rect)
                    is Outline.Rounded -> canvas.clipRoundRect(outline.roundRect)
                    is Outline.Generic -> canvas.clipPath(outline.path)
                }
            }

            drawBlock(canvas)
            canvas.restore()
        }

         */
    }

    override fun updateDisplayList() = Unit

    @OptIn(ExperimentalUnsignedTypes::class)
    fun drawShadow(canvas: SkiaNativeCanvas): Any = TODO("implement draw shadow")
    /*
    = with(getDensity()) {
        val path = when (val outline = outlineCache.outline) {
            is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
            is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
            is Outline.Generic -> outline.path
            else -> return
        }

        // TODO: perspective?
        val zParams = Point3(0f, 0f, shadowElevation)

        // TODO: configurable?
        val lightPos = Point3(0f, -300.dp.toPx(), 600.dp.toPx())
        val lightRad = 800.dp.toPx()

        val ambientAlpha = 0.039f * alpha
        val spotAlpha = 0.19f * alpha
        val ambientColor = Color.Black.copy(alpha = ambientAlpha)
        val spotColor = Color.Black.copy(alpha = spotAlpha)

        ShadowUtils.drawShadow(
            canvas.nativeCanvas, path.asNativePath(), zParams, lightPos,
            lightRad,
            ambientColor.toArgb(),
            spotColor.toArgb(), alpha < 1f, false
        )
    }

     */

    companion object {
        private var lastId = 0L
    }
}
