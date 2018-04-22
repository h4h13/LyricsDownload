package code.name.monkey.companion

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.support.annotation.Nullable
import code.name.monkey.companion.mvp.model.Song
import io.reactivex.Observable


/**
 * @author Hemanth S (h4h13).
 */

object SongLoader {

    private val BASE_SELECTION = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''"
    private val BASE_PROJECTION = arrayOf(BaseColumns._ID, // 0
            AudioColumns.TITLE, // 1
            AudioColumns.TRACK, // 2
            AudioColumns.YEAR, // 3
            AudioColumns.DURATION, // 4
            AudioColumns.DATA, // 5
            AudioColumns.DATE_MODIFIED, // 6
            AudioColumns.ALBUM_ID, // 7
            AudioColumns.ALBUM, // 8
            AudioColumns.ARTIST_ID, // 9
            AudioColumns.ARTIST)// 10

    fun getAllSongs(context: Context): Observable<ArrayList<Song>> {
        val cursor = makeSongCursor(context)
        return getSongs(cursor)
    }

    fun getSongs(cursor: Cursor?): Observable<ArrayList<Song>> {
        return Observable.create({ e ->
            run {
                val songs = ArrayList<Song>()
                if (cursor!!.moveToFirst()) {
                    do {
                        songs.add(getSongFromCursorImpl(cursor))
                    } while (cursor.moveToNext())
                }
                cursor.close()
                e.onNext(songs)
                e.onComplete()
            }
        })
    }

    fun getSong(context: Context, queryId: Int): Song {
        val cursor = makeSongCursor(context, AudioColumns._ID + "=?", arrayOf(queryId.toString()))
        return getSong(cursor)
    }

    fun getSong(cursor: Cursor?): Song {
        val song: Song
        if (cursor != null && cursor.moveToFirst()) {
            song = getSongFromCursorImpl(cursor)
        } else {
            song = Song.EMPTY_SONG
        }
        cursor?.close()
        return song
    }

    private fun getSongFromCursorImpl(cursor: Cursor): Song {
        val id = cursor.getInt(0)
        val title = cursor.getString(1)
        val trackNumber = cursor.getInt(2)
        val year = cursor.getInt(3)
        val duration = cursor.getLong(4)
        val data = cursor.getString(5)
        val dateModified = cursor.getLong(6)
        val albumId = cursor.getInt(7)
        val albumName = cursor.getString(8)
        val artistId = cursor.getInt(9)
        val artistName = cursor.getString(10)

        return Song(id, title, trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName)
    }

    @Nullable
    fun makeSongCursor(context: Context, @Nullable selection: String? = null, selectionValues: Array<String>? = null): Cursor? {
        return makeSongCursor(context, selection, selectionValues, MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
    }

    @Nullable
    private fun makeSongCursor(context: Context, @Nullable selectionString: String?,
                               selectionValues: Array<String>?, sortOrder: String): Cursor? {
        var selection = selectionString
        if (selection != null && selection.trim { it <= ' ' } != "") {
            selection = "$BASE_SELECTION AND $selection"
        } else {
            selection = BASE_SELECTION
        }

        try {
            return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION, selection, selectionValues, sortOrder)
        } catch (e: SecurityException) {
            return null
        }

    }
}