package im.threads.business.markdown

import android.graphics.Typeface

class MarkdownConfig {
    /**
     * Color of links in text.
     * TextPaint#linkColor will be used as a default linkColor. @ColorRes
     */
    var linkColor: Int? = null

    /**
     * Whether links should be underlined. The default value is false.
     */
    var isLinkUnderlined: Boolean = false

    /**
     * Starting margin before text content for the lists, blockquotes, task lists. In pixels.
     * Default margin is 24dp converted to pixels.
     */
    var blockMargin: Int? = null

    /**
     * Width of a blockquote stripe. In pixels. Default: 1/4 of the block margin
     */
    var blockQuoteWidth: Int? = null

    /**
     *  Color of a blockquote stripe. Default: textColor with 25 (0-255) alpha value. @ColorRes
     */
    var blockQuoteColor: Int? = null

    /**
     *  Controls the color of a list item.
     *  For ordered list: leading number, for unordered list: bullet. @ColorRes
     *  Default: text color
     */
    var listItemColor: Int? = null

    /**
     *  Border width of a bullet list item (level 2). In pixels.
     */
    var bulletListItemStrokeWidth: Int? = null

    /**
     *  The width of the bullet item. In pixels.
     *  Default: min(blockMargin, lineHeight) / 2
     */
    var bulletWidth: Int? = null

    /**
     *  The color of the code content. @ColorRes
     *  Default: сontent text color
     */
    var codeTextColor: Int? = null

    /**
     *  The color of background of a code content. @ColorRes
     *  Default: inline code text color with 25 (0-255) alpha
     */
    var codeBackgroundColor: Int? = null

    /**
     *  The color of the text inside the block. @ColorRes
     *  Default: inline code text color
     */
    var codeBlockTextColor: Int? = null

    /**
     *  The color of background of code block text. @ColorRes
     *  Default: inline code background color
     */
    var codeBlockBackgroundColor: Int? = null

    /**
     * Typeface of code content. android.graphics.Typeface.
     * Default: Typeface.MONOSPACE
     */
    var codeTypeface: Typeface? = null

    /**
     * Typeface of block code content. android.graphics.Typeface.
     * Default: codeTypeface if set or Typeface.MONOSPACE
     */
    var codeBlockTypeface: Typeface? = null

    /**
     * Text size of code content. In pixels.
     * Default: (Content text size) * 0.87 if no custom Typeface was set,
     * otherwise (content text size)
     */
    var codeTextSize: Int? = null

    /**
     * Text size of block code content. In pixels.
     * Default: codeTextSize if set or (content text size) * 0.87 if no custom Typeface was set,
     * otherwise (content text size)
     */
    var codeBlockTextSize: Int? = null

    /**
     * The height of a brake under H1 & H2. In pixels.
     * Default: Stroke width of context TextPaint
     */
    var headingBreakHeight: Int? = null

    /**
     * The color of a brake under H1 & H2. @ColorRes.
     * Default: (text color) with 75 (0-255) alpha
     */
    var headingBreakColor: Int? = null

    /**
     * The typeface of heading elements. @android.graphics.Typeface.
     * Default: default text Typeface
     */
    var headingTypeface: Typeface? = null

    /**
     * Array of heading text sizes ratio that is applied to text size. float[]
     * Default: {2.F, 1.5F, 1.17F, 1.F, .83F, .67F} (HTML spec)
     */
    var headingTextSizeMultipliers: FloatArray? = null

    /**
     * Color of a thematic break. @ColorRes.
     * Default: (text color) with 25 (0-255) alpha
     */
    var thematicBreakColor: Int? = null

    /**
     * Height of a thematic break. In pixels.
     * Default: Stroke width of context TextPaint
     */
    var thematicBreakHeight: Int? = null
}
