package code.name.monkey.companion.util

import android.graphics.Bitmap
import android.support.annotation.ColorInt
import android.support.v7.graphics.Palette
import java.util.*


object RetroColorUtil {

    fun generatePalette(bitmap: Bitmap?): Palette? {
        return if (bitmap == null) null else Palette.from(bitmap).generate()
    }

    @ColorInt
    fun getColor(palette: Palette?, fallback: Int): Int {
        if (palette != null) {
            if (palette.vibrantSwatch != null) {
                return palette.vibrantSwatch!!.rgb
            } else if (palette.mutedSwatch != null) {
                return palette.mutedSwatch!!.rgb
            } else if (palette.darkVibrantSwatch != null) {
                return palette.darkVibrantSwatch!!.rgb
            } else if (palette.darkMutedSwatch != null) {
                return palette.darkMutedSwatch!!.rgb
            } else if (palette.lightVibrantSwatch != null) {
                return palette.lightVibrantSwatch!!.rgb
            } else if (palette.lightMutedSwatch != null) {
                return palette.lightMutedSwatch!!.rgb
            } else if (!palette.swatches.isEmpty()) {
                return Collections.max<Palette.Swatch>(palette.swatches, SwatchComparator.instance).getRgb()
            }
        }
        return fallback
    }

    private class SwatchComparator : Comparator<Palette.Swatch> {

        override fun compare(lhs: Palette.Swatch, rhs: Palette.Swatch): Int {
            return lhs.population - rhs.population
        }

        companion object {
            private var sInstance: SwatchComparator? = null

            internal val instance: SwatchComparator
                get() {
                    if (sInstance == null) {
                        sInstance = SwatchComparator()
                    }
                    return sInstance as SwatchComparator
                }
        }
    }
}
