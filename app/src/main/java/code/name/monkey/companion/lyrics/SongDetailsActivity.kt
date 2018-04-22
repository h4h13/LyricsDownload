package code.name.monkey.companion.lyrics

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import code.name.monkey.companion.R
import code.name.monkey.companion.SongLoader
import code.name.monkey.companion.lyrics.wiki.LyricsWikiEngine
import code.name.monkey.companion.mvp.model.LoadingInfo
import code.name.monkey.companion.mvp.model.Song
import code.name.monkey.companion.util.LyricUtil
import code.name.monkey.companion.util.MusicUtil
import kotlinx.android.synthetic.main.activity_song_details.*
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SongDetailsActivity : AppCompatActivity() {

    lateinit var song: Song
    lateinit var lyricsWikiEngine: LyricsWikiEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_details)

        if (intent.getParcelableExtra<Song>("song") == null) {
            return
        }

        song = intent.getParcelableExtra("song")

        songTitle.setText(song.title)
        songArtist.setText(song.artistName)

        if (MusicUtil.getLyrics(song).isNullOrEmpty()) {
            LyricsFetcher().execute()
        } else {
            lyricsShow.setText(MusicUtil.getLyrics(song))
        }

        if (LyricUtil.isLrcFileExist(song.title, song.artistName)) {
            syncLyrics.setTextColor(Color.GREEN)
        }

        save.setOnClickListener({
            val fieldKeyValueMap = EnumMap<FieldKey, String>(FieldKey::class.java)
            fieldKeyValueMap[FieldKey.TITLE] = songTitle.text.toString()
            fieldKeyValueMap[FieldKey.ARTIST] = songArtist.text.toString()
            fieldKeyValueMap[FieldKey.LYRICS] = lyricsShow.text.toString()
            WriteTagsAsyncTask(this@SongDetailsActivity)
                    .execute(LoadingInfo(getSongPaths(), fieldKeyValueMap, null))

        })
        done.setOnClickListener({
            onBackPressed()
        })
        searchButton.setOnClickListener({
            searchWebFor(songTitle.text.toString(), songArtist.text.toString(), " Lyrics")
        })
        syncLyrics.setOnClickListener({
            KogouLyricsFetcher(object : KogouLyricsFetcher.KogouLyricsCallback {
                override fun onNoLyrics() {
                    Toast.makeText(applicationContext, "NO Downloaded", Toast.LENGTH_SHORT).show()
                }

                override fun onLyrics(file: File?) {
                    Toast.makeText(applicationContext, "Downloaded", Toast.LENGTH_SHORT).show()
                }
            }).loadLyrics(song, "0")
        })
    }

    private fun searchWebFor(vararg keys: String) {
        val stringBuilder = StringBuilder()
        for (key in keys) {
            stringBuilder.append(key)
            stringBuilder.append(" ")
        }
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when {
            item?.itemId == android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getSongPaths(): List<String> {
        val paths = ArrayList<String>(1)
        paths.add(SongLoader.getSong(this, getId()).data)
        return paths
    }

    private fun getId(): Int {
        return song.id
    }

    @SuppressLint("StaticFieldLeak")
    inner class LyricsFetcher : AsyncTask<Void, Void, String?>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar2.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void): String? {
            lyricsWikiEngine = LyricsWikiEngine()
            return lyricsWikiEngine.getLyrics(song.artistName, song.title)
        }

        override fun onPostExecute(lyrics: String?) {
            progressBar2.visibility = View.GONE
            if (TextUtils.isEmpty(lyrics)) {
                // no lyrics - show excuse
                Toast.makeText(this@SongDetailsActivity, R.string.lyrics_not_found, Toast.LENGTH_SHORT).show()
            }
            showFetchedLyrics(lyrics)
        }
    }

    private fun showFetchedLyrics(lyrics: String?) {
        lyricsShow.setText(lyrics)
    }
}