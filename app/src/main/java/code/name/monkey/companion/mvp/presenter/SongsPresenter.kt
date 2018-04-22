package code.name.monkey.companion.mvp.presenter

import android.content.Context
import code.name.monkey.companion.SongLoader
import code.name.monkey.companion.mvp.Presenter
import code.name.monkey.companion.mvp.contract.SongsContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Hemanth S (h4h13).
 */
class SongsPresenter(private val context: Context, val view: SongsContract.View?) : Presenter(), SongsContract.Presenter {
    override fun subscribe() {
        loadSongs()
    }

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun loadSongs() {
        disposable.add(SongLoader.getAllSongs(context)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.let {
                        view!!.showResult(it)
                    }
                }, {
                    view!!.error()
                }, {
                    view!!.complete()
                }))
    }
}