package im.threads.internal.image_loading

import android.graphics.drawable.Drawable

sealed class ImageModifications {
    /**
     * Делает из картинки круг и применяет на ней маску "center crop"
     */
    object CircleCropModification : ImageModifications()

    /**
     * Применяет произвольную маску на изображении
     * @param maskDrawable маска типа NinePatchDrawable
     */
    class MaskedModification(val maskDrawable: Drawable) : ImageModifications()
}
