package code.name.monkey.companion.mvp

import io.reactivex.disposables.CompositeDisposable

/**
 * @author Hemanth S (h4h13).
 */
open class Presenter {
    protected var disposable: CompositeDisposable = CompositeDisposable()
}