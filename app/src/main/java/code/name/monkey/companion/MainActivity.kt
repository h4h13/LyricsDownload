package code.name.monkey.companion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import code.name.monkey.companion.mvp.contract.SongsContract
import code.name.monkey.companion.mvp.model.Song
import code.name.monkey.companion.mvp.presenter.SongsPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), SongsContract.View, TextWatcher {
    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        filter(s.toString())
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        filter(s.toString())
    }

    private var songsPresenter: SongsPresenter? = null
    lateinit var adapter: SongsAdapter

    override fun loading() {

    }

    override fun error() {

    }

    override fun complete() {
    }

    override fun showResult(result: ArrayList<Song>) {
        originalList = result
        adapter.swapData(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        adapter = SongsAdapter(this)
        songList.adapter = adapter
        songList.layoutManager = LinearLayoutManager(this)

        songsPresenter = SongsPresenter(this, this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Batch download coming soon", Snackbar.LENGTH_LONG)
                    .show()
        }
        search.addTextChangedListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (setupPermission()) {
            songsPresenter!!.subscribe()
        }
    }

    override fun onPause() {
        super.onPause()
        songsPresenter!!.unsubscribe()
    }

    private fun setupPermission(): Boolean {
        val permissionRead = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionWrite = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val permissionNeed = ArrayList<String>()

        if (permissionRead != PackageManager.PERMISSION_GRANTED) {
            permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
            permissionNeed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionNeed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionNeed.toTypedArray(), 100)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                songsPresenter!!.subscribe()
            }
        }
    }

    lateinit var originalList: ArrayList<Song>
    fun filter(text: String) {
        val temp = ArrayList<Song>()
        if (text.isEmpty()) {
            temp.addAll(originalList)
            adapter.swapData(temp)
            return
        }
        for (d in originalList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.title.toLowerCase().contains(text) || d.albumName.toLowerCase().contains(text)) {
                temp.add(d)
            }
        }
        //update recyclerview
        adapter.swapData(temp)
    }
}
