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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.NativePath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
// import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
/*
import org.jetbrains.skija.Paint
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.paragraph.Alignment as SkAlignment
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.Direction as SkDirection
import org.jetbrains.skija.paragraph.LineMetrics
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.RectHeightMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.TextBox
import java.lang.UnsupportedOperationException
import java.util.WeakHashMap

 */
import kotlin.math.floor
/*
import org.jetbrains.skija.Rect as SkRect
import org.jetbrains.skija.paragraph.Paragraph as SkParagraph
import org.jetbrains.skija.paragraph.TextStyle as SkTextStyle
import org.jetbrains.skija.FontStyle as SkFontStyle
import org.jetbrains.skija.Font as SkFont
import org.jetbrains.skija.paragraph.DecorationLineStyle as SkDecorationLineStyle
import org.jetbrains.skija.paragraph.DecorationStyle as SkDecorationStyle
import org.jetbrains.skija.paragraph.Shadow as SkShadow
*/
import org.jetbrains.skiko.skia.native.Paragraph as SkParagraph
import org.jetbrains.skiko.skia.native.ParagraphBuilder as SkParagraphBuilder
import org.jetbrains.skiko.skia.native.TextStyle as SkTextStyle
import org.jetbrains.skiko.skia.native.SkFontStyle
import org.jetbrains.skiko.skia.native.skia__textlayout__ParagraphStyle as ParagraphStyle
import org.jetbrains.skiko.skia.native.SkFont
import org.jetbrains.skiko.skia.native.SkFontMetrics
import org.jetbrains.skiko.skia.native.*
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.readValue
import kotlinx.cinterop.ptr

private val DefaultFontSize = 16.sp

internal actual fun ActualParagraph(
    text: String,
    style: TextStyle,
    spanStyles: List<Range<SpanStyle>>,
    placeholders: List<Range<Placeholder>>,
    maxLines: Int,
    ellipsis: Boolean,
    width: Float,
    density: Density,
    resourceLoader: Font.ResourceLoader
): Paragraph = NativeParagraph(
    paragraphIntrinsics = NativeParagraphIntrinsics(
        text = text,
        style = style,
        spanStyles = spanStyles,
        placeholders = placeholders,
        density = density,
        resourceLoader = resourceLoader
    ),
    maxLines = maxLines,
    ellipsis = ellipsis,
    width = width
)

@Suppress("UNUSED_PARAMETER")
internal actual fun ActualParagraph(
    paragraphIntrinsics: ParagraphIntrinsics,
    maxLines: Int,
    ellipsis: Boolean,
    width: Float
): Paragraph = NativeParagraph(
    paragraphIntrinsics = paragraphIntrinsics,
    maxLines = maxLines,
    ellipsis = ellipsis,
    width = width
)


internal class NativeParagraph(
    paragraphIntrinsics: ParagraphIntrinsics,
    val maxLines: Int,
    val ellipsis: Boolean,
    override val width: Float
) : Paragraph
{

    init {
//        if (resetMaxLinesIfNeeded()) {
//            rebuildParagraph()
//        }
//        para.layout(width)
    }

    val nativeParagraphIntrinsics = paragraphIntrinsics as NativeParagraphIntrinsics


    //val para: SkParagraph
    //    get() = paragraphIntrinsics.para

    override val height: Float
        get() = 2.0f//TODO("Not yet implemented")

    override val minIntrinsicWidth: Float
        get() = nativeParagraphIntrinsics.minIntrinsicWidth

    override val maxIntrinsicWidth: Float
        get() = nativeParagraphIntrinsics.maxIntrinsicWidth

    override val firstBaseline: Float
        get() = 0f //TODO("Not yet implemented")

    override val lastBaseline: Float
        get() = 0f //TODO("Not yet implemented")

    override val didExceedMaxLines: Boolean
        get() = false //TODO("Not yet implemented")

    override val lineCount: Int
        get() = 1 //TODO("Not yet implemented")

    override val placeholderRects: List<Rect?>
        get() = emptyList()//TODO("Not yet implemented")

    override fun getPathForRange(start: Int, end: Int): Path {
        TODO("Not yet implemented")
    }

    override fun getCursorRect(offset: Int): Rect {
        TODO("Not yet implemented")
    }

    override fun getLineLeft(lineIndex: Int): Float {
        TODO("Not yet implemented")
    }

    override fun getLineRight(lineIndex: Int): Float {
        TODO("Not yet implemented")
    }

    override fun getLineTop(lineIndex: Int): Float {
        TODO("Not yet implemented")
    }

    override fun getLineBottom(lineIndex: Int): Float {
        TODO("Not yet implemented")
    }

    override fun getLineHeight(lineIndex: Int): Float {
        TODO("Not yet implemented")
    }

    override fun getLineWidth(lineIndex: Int): Float {
        TODO("Not yet implemented")
    }

    override fun getLineStart(lineIndex: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getLineEnd(lineIndex: Int, visibleEnd: Boolean): Int {
        TODO("Not yet implemented")
    }

    override fun isLineEllipsized(lineIndex: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLineForOffset(offset: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getHorizontalPosition(offset: Int, usePrimaryDirection: Boolean): Float {
        TODO("Not yet implemented")
    }

    override fun getParagraphDirection(offset: Int): ResolvedTextDirection {
        TODO("Not yet implemented")
    }

    override fun getBidiRunDirection(offset: Int): ResolvedTextDirection {
        TODO("Not yet implemented")
    }

    override fun getLineForVerticalPosition(vertical: Float): Int {
        TODO("Not yet implemented")
    }

    override fun getOffsetForPosition(position: Offset): Int {
        TODO("Not yet implemented")
    }

    override fun getBoundingBox(offset: Int): Rect {
        TODO("Not yet implemented")
    }

    override fun getWordBoundary(offset: Int): TextRange {
        TODO("Not yet implemented")
    }

    override fun paint(canvas: Canvas, color: Color, shadow: Shadow?, textDecoration: TextDecoration?) {
        TODO("Not yet implemented")
    }
}

private fun fontSizeInHierarchy(density: Density, base: Float, other: TextUnit): Float {
    return when {
        other.isUnspecified -> base
        other.isEm -> base * other.value
        other.isSp -> with(density) { other.toPx() }
        else -> throw UnsupportedOperationException()
    }
}

// Computed ComputedStyles always have font/letter size in pixels for particular `density`.
// It's important because density could be changed in runtime and it should force
// SkTextStyle to be recalculated. Or we can have different densities in different windows.
private data class ComputedStyle(
    var color: Color,
    var fontSize: Float,
    var fontWeight: FontWeight?,
    var fontStyle: androidx.compose.ui.text.font.FontStyle?,
    var fontSynthesis: FontSynthesis?,
    var fontFamily: FontFamily?,
    var fontFeatureSettings: String?,
    var letterSpacing: Float?,
    var baselineShift: BaselineShift?,
    var textGeometricTransform: TextGeometricTransform?,
    var localeList: LocaleList?,
    var background: Color = Color.Unspecified,
    var textDecoration: TextDecoration?,
    var shadow: Shadow?
)
{

    constructor(density: Density, spanStyle: SpanStyle) : this(
        color = spanStyle.color,
        fontSize = with(density) { spanStyle.fontSize.toPx() },
        fontWeight = spanStyle.fontWeight,
        fontStyle = spanStyle.fontStyle,
        fontSynthesis = spanStyle.fontSynthesis,
        fontFamily = spanStyle.fontFamily,
        fontFeatureSettings = spanStyle.fontFeatureSettings,
        letterSpacing = if (spanStyle.letterSpacing.isUnspecified) {
            null
        } else {
            with(density) {
                spanStyle.letterSpacing.toPx()
            }
        },
        baselineShift = spanStyle.baselineShift,
        textGeometricTransform = spanStyle.textGeometricTransform,
        localeList = spanStyle.localeList,
        background = spanStyle.background,
        textDecoration = spanStyle.textDecoration,
        shadow = spanStyle.shadow
    )

    fun toSkTextStyle(fontLoader: FontLoader): SkTextStyle {
        val res = SkTextStyle()
        if (color != Color.Unspecified) {
//            res.setColor(color.toArgb())
            res.setColor(SK_ColorDKGRAY)
        }
        fontFamily?.let {
//            val fontFamilies = fontLoader.ensureRegistered(it)
//            res.setFontFamilies(fontFamilies.toTypedArray())
        }
        fontStyle?.let {
            res.setFontStyle(it.toSkFontStyle())
        }
        textDecoration?.let {
            //res.decorationStyle = it.toSkDecorationStyle(this.color)
        }
        if (background != Color.Unspecified) {
//            res.background = Paint().also {
//                it.color = background.toArgb()
//            }
        }
        fontWeight?.let {
            //res.fontStyle = res.fontStyle.withWeight(it.weight)
        }
        shadow?.let {
            //res.addShadow(it.toSkShadow())
        }

        letterSpacing?.let {
            res.setLetterSpacing(it)
        }

        res.setFontSize(fontSize)
        return res
    }

    fun merge(density: Density, other: SpanStyle) {
        val fontSize = fontSizeInHierarchy(density, fontSize, other.fontSize)
        if (other.color.isSpecified) {
            color = other.color
        }
        other.fontFamily?.let { fontFamily = it }
        this.fontSize = fontSize
        other.fontWeight?.let { fontWeight = it }
        other.fontStyle?.let { fontStyle = it }
        other.fontSynthesis?.let { fontSynthesis = it }
        other.fontFeatureSettings?.let { fontFeatureSettings = it }
        if (!other.letterSpacing.isUnspecified) {
            when {
                other.letterSpacing.isEm ->
                    letterSpacing = fontSize * other.letterSpacing.value
                other.letterSpacing.isSp ->
                    letterSpacing = with(density) {
                        other.letterSpacing.toPx()
                    }
                else -> throw UnsupportedOperationException()
            }
        }
        other.baselineShift?.let { baselineShift = it }
        other.textGeometricTransform?.let { textGeometricTransform = it }
        other.localeList?.let { localeList = it }
        if (other.background.isSpecified) {
            background = other.background
        }
        other.textDecoration?.let { textDecoration = it }
        other.shadow?.let { shadow = it }
    }
}

fun androidx.compose.ui.text.font.FontStyle.toSkFontStyle(): CValue<SkFontStyle> {
    return when (this) {
        androidx.compose.ui.text.font.FontStyle.Italic -> SkFontStyle.Italic()
        else -> SkFontStyle.Normal()
    }
}

private fun TextUnit.orDefaultFontSize() = when {
    isUnspecified -> DefaultFontSize
    isEm -> DefaultFontSize * value
    else -> this
}

private fun SpanStyle.withDefaultFontSize(): SpanStyle {
    val fontSize = this.fontSize.orDefaultFontSize()
    val letterSpacing = when {
        this.letterSpacing.isEm -> fontSize * this.letterSpacing.value
        else -> this.letterSpacing
    }
    return this.copy(
        fontSize = fontSize,
        letterSpacing = letterSpacing
    )
}

internal class ParagraphBuilder(
    val fontLoader: FontLoader,
    val text: String,
    var textStyle: TextStyle,
    var ellipsis: String = "",
    var maxLines: Int = Int.MAX_VALUE,
    val spanStyles: List<Range<SpanStyle>>,
    val placeholders: List<Range<Placeholder>>,
    val density: Density,
    val textDirection: ResolvedTextDirection
) {
    private lateinit var initialStyle: SpanStyle
    private lateinit var defaultStyle: ComputedStyle
    private lateinit var ops: List<Op>

    /**
     * SkParagraph styles model doesn't match Compose's one.
     * SkParagraph has only a stack-based push/pop styles interface that works great with Span
     * trees.
     * But in Compose we have a list of SpanStyles attached to arbitrary ranges, possibly
     * overlapped, where a position in the list denotes style's priority
     * We map Compose styles to SkParagraph styles by projecting every range start/end to single
     * positions line and maintaining a list of active styles while building a paragraph. This list
     * of active styles is being compiled into single SkParagraph's style for every chunk of text
     */
    fun build(): SkParagraph {
        initialStyle = textStyle.toSpanStyle().withDefaultFontSize()
        defaultStyle = ComputedStyle(density, initialStyle)
        ops = makeOps(
            spanStyles,
            placeholders
        )

        var pos = 0
        val ps: ParagraphStyle = textStyleToParagraphStyle(textStyle)

        if (maxLines != Int.MAX_VALUE) {
            ps.setMaxLines(maxLines.toULong())
            //ps.setEllipsis(ellipsis)
        }

        val cvaluesRefParagraphStyle:  CValuesRef<ParagraphStyle> = ps.readValue()
        val cPointerFonts: CPointer<skia__textlayout__FontCollection> = fontLoader.fonts.ptr

        val pb = SkParagraphBuilder(cvaluesRefParagraphStyle, cPointerFonts)

        var addText = true

        for (op in ops) {
            if (addText && pos < op.position) {
                pb.addText(text.subSequence(pos, op.position).toString())
            }

            when (op) {
                is Op.StyleAdd -> {
                    // cached SkTextStyled could was loaded with a different font loader
                    //ensureFontsAreRegistered(fontLoader, op.style)
                    //pb.pushStyle(makeSkTextStyle(op.style))
                }
                is Op.PutPlaceholder -> {
//                    val placeholderStyle =
//                        PlaceholderStyle(
//                            op.width,
//                            op.height,
//                            op.cut.placeholder.placeholderVerticalAlign
//                                .toSkPlaceholderAlignment(),
//                            // TODO: figure out how exactly we have to work with BaselineMode & offset
//                            BaselineMode.ALPHABETIC,
//                            0f
//                        )
//                    pb.addPlaceholder(placeholderStyle)
                    addText = false
                }
                is Op.EndPlaceholder -> {
                    addText = true
                }
            }

            pos = op.position
        }

        if (addText && pos < text.length) {
            pb.addText(text.subSequence(pos, text.length).toString())
        }

        return pb.build()
    }

    private fun ensureFontsAreRegistered(fontLoader: FontLoader, style: ComputedStyle) {
        style.fontFamily?.let {
            fontLoader.ensureRegistered(it)
        }
    }

    private sealed class Op {
        abstract val position: Int

        data class StyleAdd(
            override val position: Int,
            val style: ComputedStyle
        ) : Op()

        data class PutPlaceholder(
            val cut: Cut.PutPlaceholder,
            var width: Float,
            var height: Float
        ) : Op() {
            override val position: Int by cut::position
        }

        data class EndPlaceholder(
            val cut: Cut.EndPlaceholder
        ) : Op() {
            override val position: Int by cut::position
        }
    }

    private sealed class Cut {
        abstract val position: Int

        data class StyleAdd(
            override val position: Int,
            val style: SpanStyle
        ) : Cut()

        data class StyleRemove(
            override val position: Int,
            val style: SpanStyle
        ) : Cut()

        data class PutPlaceholder(
            override val position: Int,
            val placeholder: Placeholder,
        ) : Cut()

        data class EndPlaceholder(override val position: Int) : Cut()
    }

    private fun makeOps(
        spans: List<Range<SpanStyle>>,
        placeholders: List<Range<Placeholder>>
    ): List<Op> {
        val cuts = mutableListOf<Cut>()
        for (span in spans) {
            cuts.add(Cut.StyleAdd(span.start, span.item))
            cuts.add(Cut.StyleRemove(span.end, span.item))
        }

        for (placeholder in placeholders) {
            cuts.add(Cut.PutPlaceholder(placeholder.start, placeholder.item))
            cuts.add(Cut.EndPlaceholder(placeholder.end))
        }

        val ops = mutableListOf<Op>(Op.StyleAdd(0, defaultStyle))
        cuts.sortBy { it.position }
        val activeStyles = mutableListOf(initialStyle)
        for (cut in cuts) {
            when {
                cut is Cut.StyleAdd -> {
                    activeStyles.add(cut.style)
                    val prev = previousStyleAddAtTheSamePosition(cut.position, ops)
                    if (prev == null) {
                        ops.add(
                            Op.StyleAdd(
                                cut.position,
                                mergeStyles(activeStyles).also { it.merge(density, cut.style) }
                            )
                        )
                    } else {
                        prev.style.merge(density, cut.style)
                    }
                }
                cut is Cut.StyleRemove -> {
                    activeStyles.remove(cut.style)
                    ops.add(Op.StyleAdd(cut.position, mergeStyles(activeStyles)))
                }
                cut is Cut.PutPlaceholder -> {
                    val currentStyle = mergeStyles(activeStyles)
                    val op = Op.PutPlaceholder(
                        cut = cut,
                        width = fontSizeInHierarchy(
                            density,
                            currentStyle.fontSize,
                            cut.placeholder.width
                        ),
                        height = fontSizeInHierarchy(
                            density,
                            currentStyle.fontSize,
                            cut.placeholder.height
                        ),
                    )
                    ops.add(op)
                }
                cut is Cut.EndPlaceholder ->
                    ops.add(Op.EndPlaceholder(cut))
            }
        }
        return ops
    }

    private fun mergeStyles(activeStyles: List<SpanStyle>): ComputedStyle {
        // there is always at least one active style
        val style = ComputedStyle(density, activeStyles[0])
        for (i in 1 until activeStyles.size) {
            style.merge(density, activeStyles[i])
        }
        return style
    }

    private fun previousStyleAddAtTheSamePosition(position: Int, ops: List<Op>): Op.StyleAdd? {
        for (prevOp in ops.asReversed()) {
            if (prevOp.position < position) return null
            if (prevOp is Op.StyleAdd) return prevOp
        }
        return null
    }

    private fun textStyleToParagraphStyle(style: TextStyle): ParagraphStyle {
        val pStyle = ParagraphStyle()
//        style.textAlign?.let {
//            pStyle.alignment = it.toSkAlignment()
//        }
//
//        if (style.lineHeight.isSpecified) {
//            val strutStyle = StrutStyle()
//
//            strutStyle.isEnabled = true
//            strutStyle.isHeightOverridden = true
//            val fontSize = with(density) {
//                style.fontSize.orDefaultFontSize().toPx()
//            }
//            val lineHeight = when {
//                style.lineHeight.isSp -> with(density) {
//                    style.lineHeight.toPx()
//                }
//                style.lineHeight.isEm -> fontSize * style.lineHeight.value
//                else -> throw IllegalStateException()
//            }
//            strutStyle.height = lineHeight / fontSize
//            pStyle.strutStyle = strutStyle
//        }
//        pStyle.direction = textDirection.toSkDirection()
        return pStyle
    }

    private fun makeSkTextStyle(style: ComputedStyle): SkTextStyle {
        return style.toSkTextStyle(fontLoader)
//        return skTextStylesCache.getOrPut(style) {
//            style.toSkTextStyle(fontLoader)
//        }
    }

    internal val defaultFont by lazy {
//        val typeface = textStyle.fontFamily?.let {
//            fontLoader.findTypeface(
//                fontFamily = it,
//                textStyle.fontWeight ?: FontWeight.Normal(),
//                textStyle.fontStyle ?: FontStyle.Normal()
//            )
//        } ?: SkTypeface.MakeDefault()
        val typeface = SkTypeface.MakeDefault()
        SkFont(typeface, defaultStyle.fontSize)
    }

    internal val defaultHeight by lazy {
        //defaultFont.getMetrics(null)
        0
    }
}
