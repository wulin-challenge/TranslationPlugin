@file:Suppress("unused")

package cn.yiiguxing.plugin.translate.service

import cn.yiiguxing.plugin.translate.ui.NewTranslationDialog
import cn.yiiguxing.plugin.translate.ui.TranslationBalloon
import cn.yiiguxing.plugin.translate.ui.wordbook.WordOfTheDayDialog
import cn.yiiguxing.plugin.translate.util.checkDispatchThread
import cn.yiiguxing.plugin.translate.wordbook.WordBookItem
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Ref
import com.intellij.util.ui.PositionTracker

/**
 * TranslationUIManager
 */
class TranslationUIManager private constructor() : Disposable {

    private var balloonRef: Ref<TranslationBalloon> = Ref.create()
    private var newTranslationDialogRef: Ref<NewTranslationDialog> = Ref.create()
    private var wordOfTheDayDialogRef: Ref<WordOfTheDayDialog> = Ref.create()

    /**
     * 关闭显示中的气泡和对话框
     */
    private fun disposeUI() {
        checkThread()
        balloonRef.get()?.hide()
        newTranslationDialogRef.get()?.close()
    }

    override fun dispose() {
        disposeUI()
    }

    fun currentBalloon(): TranslationBalloon? = balloonRef.get()

    fun currentNewTranslationDialog(): NewTranslationDialog? = newTranslationDialogRef.get()

    companion object {

        private inline fun <T> Ref<T>.getOrPut(create: () -> T): T {
            val cached = get()
            if (cached != null)
                return cached

            val created = create()
            set(created)
            return created
        }

        private fun <T> Ref<T>.clearOnDispose(disposable: Disposable) {
            Disposer.register(disposable, {
                checkThread()
                set(null)
            })
        }

        fun instance(project: Project?): TranslationUIManager {
            return if (project == null)
                ServiceManager.getService(TranslationUIManager::class.java)
            else
                ServiceManager.getService(project, TranslationUIManager::class.java)
        }

        /**
         * Project or application should not be used as parent disposables for plugin classes
         * https://jetbrains.org/intellij/sdk/docs/basics/disposers.html#choosing-a-disposable-parent
         */
        fun disposable(project: Project? = null): Disposable = instance(project)

        private inline fun <D : DialogWrapper> showDialog(
            cache: Ref<D>,
            onBeforeShow: (D) -> Unit = {},
            dialog: () -> D
        ): D {
            checkThread()
            return cache.getOrPut {
                dialog().also {
                    cache.clearOnDispose(it.disposable)
                }
            }.also {
                onBeforeShow(it)
                it.show()
            }
        }

        /**
         * 显示气泡
         *
         * @param editor   编辑器
         * @param text     查询字符串
         * @param tracker  位置跟踪器
         * @param position 气泡位置
         * @return 气泡实例
         */
        fun showBalloon(editor: Editor, text: String, tracker: PositionTracker<Balloon>, position: Balloon.Position)
                : TranslationBalloon {
            checkThread()
            val project = editor.project
            val uiManager = instance(project)
            uiManager.balloonRef.get()?.hide()

            return TranslationBalloon(editor, text).also {
                uiManager.balloonRef.set(it)
                uiManager.balloonRef.clearOnDispose(it)
                it.show(tracker, position)
            }
        }

        /**
         * 显示对话框
         *
         * @return 对话框实例
         */
        fun showDialog(project: Project?): NewTranslationDialog {
            return showDialog(instance(project).newTranslationDialogRef) {
                NewTranslationDialog(project)
            }
        }

        /**
         * 显示每日单词对话框
         *
         * @return 对话框实例
         */
        fun showWordOfTheDayDialog(project: Project?, words: List<WordBookItem>): WordOfTheDayDialog {
            return showDialog(instance(project).wordOfTheDayDialogRef, { it.setWords(words) }) {
                WordOfTheDayDialog(project, words)
            }
        }


        private fun checkThread() = checkDispatchThread(TranslationUIManager::class.java)
    }
}