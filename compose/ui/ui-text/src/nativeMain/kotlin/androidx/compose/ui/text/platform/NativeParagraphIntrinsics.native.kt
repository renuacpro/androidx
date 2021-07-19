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
package androidx.compose.ui.text.platform

import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import kotlin.math.ceil
import org.jetbrains.skiko.skia.native.Paragraph as SkParagraph

internal actual fun ActualParagraphIntrinsics(
    text: String,
    style: TextStyle,
    spanStyles: List<Range<SpanStyle>>,
    placeholders: List<Range<Placeholder>>,
    density: Density,
    resourceLoader: Font.ResourceLoader
): ParagraphIntrinsics = NativeParagraphIntrinsics(
    text = text,
    style = style,
    spanStyles = spanStyles,
    placeholders = placeholders,
    density = density,
    resourceLoader = resourceLoader
)

internal class NativeParagraphIntrinsics(
    val text: String,
    style: TextStyle,
    spanStyles: List<Range<SpanStyle>>,
    placeholders: List<Range<Placeholder>>,
    density: Density,
    resourceLoader: Font.ResourceLoader
) : ParagraphIntrinsics {

    // Todo: add skia paragraph reference, use it in NativeParagraph.native.kt
    lateinit var para: SkParagraph

    val builder: ParagraphBuilder
    val fontLoader = resourceLoader as FontLoader
    val textDirection = resolveTextDirection(style.textDirection)

    override val minIntrinsicWidth = ceil(para.getMinIntrinsicWidth()).toFloat()
    override val maxIntrinsicWidth = ceil(para.getMaxIntrinsicWidth()).toFloat()

    init {
        builder = ParagraphBuilder(
            fontLoader = fontLoader,
            text = text,
            textStyle = style,
            spanStyles = spanStyles,
            placeholders = placeholders,
            density = density,
            textDirection = textDirection
        )

        para = builder.build()
        para.layout(Float.POSITIVE_INFINITY)
    }

    private fun resolveTextDirection(direction: TextDirection?): ResolvedTextDirection {
        return when (direction) {
            TextDirection.Ltr -> ResolvedTextDirection.Ltr
            TextDirection.Rtl -> ResolvedTextDirection.Rtl
            TextDirection.Content -> contentBasedTextDirection() ?: ResolvedTextDirection.Ltr
            TextDirection.ContentOrLtr -> contentBasedTextDirection() ?: ResolvedTextDirection.Ltr
            TextDirection.ContentOrRtl -> contentBasedTextDirection() ?: ResolvedTextDirection.Rtl
            else -> ResolvedTextDirection.Ltr
        }
    }

    private fun contentBasedTextDirection(): ResolvedTextDirection? {
        // TODO: implement contentBasedTextDirection
//        for (char in text) {
//            when (char.directionality) {
//                CharDirectionality.LEFT_TO_RIGHT -> return ResolvedTextDirection.Ltr
//                CharDirectionality.RIGHT_TO_LEFT -> return ResolvedTextDirection.Rtl
//                else -> continue
//            }
//        }
        return null
    }
}
