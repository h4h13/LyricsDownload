package code.name.monkey.companion.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author Hemanth S (h4h13).
 */
fun ViewGroup.inflate(layoutId: Int, attachedToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachedToRoot)
}