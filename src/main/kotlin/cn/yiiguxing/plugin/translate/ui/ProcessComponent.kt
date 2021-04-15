@file:Suppress("unused")

package cn.yiiguxing.plugin.translate.ui

import cn.yiiguxing.plugin.translate.ui.icon.ProcessIcon
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import java.awt.Insets
import javax.swing.JPanel

/**
 * ProcessComponent
 */
class ProcessComponent(insets: Insets = JBUI.emptyInsets()) : JPanel(), Disposable {

    private val icon: ProcessIcon = ProcessIcon()

    val isRunning: Boolean get() = icon.isRunning

    init {
        isOpaque = false
        layout = GridLayoutManager(1, 1, insets, 0, 0)

        add(icon, GridConstraints().apply {
            column = 0
            hSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            anchor = GridConstraints.ANCHOR_CENTER
        })
    }

    fun resume() = icon.resume()

    fun suspend() = icon.suspend()

    override fun dispose() {
        Disposer.dispose(icon)
    }

}
