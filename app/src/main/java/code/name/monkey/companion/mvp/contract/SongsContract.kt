package code.name.monkey.companion.mvp.contract

import code.name.monkey.companion.mvp.BasePresenter
import code.name.monkey.companion.mvp.BaseView
import code.name.monkey.companion.mvp.model.Song

/**
 * @author Hemanth S (h4h13).
 */
interface SongsContract {
    interface View : BaseView<ArrayList<Song>> {

    }

    interface Presenter : BasePresenter {
        fun loadSongs()
    }
}