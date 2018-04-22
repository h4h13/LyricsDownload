package code.name.monkey.companion.mvp

/**
 * @author Hemanth S (h4h13).
 */
interface BaseView<in T> {
    fun loading()
    fun error()
    fun complete()
    fun showResult(result: T)
}