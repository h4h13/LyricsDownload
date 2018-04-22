package code.name.monkey.companion.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import code.name.monkey.companion.mvp.model.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class MusicUtil {

    companion object {
        fun insertAlbumArt(context: Context, albumId: Int, path: String) {
            val contentResolver = context.contentResolver

            val artworkUri = Uri.parse("content://media/external/audio/albumart")
            contentResolver.delete(ContentUris.withAppendedId(artworkUri, albumId.toLong()), null, null)

            val values = ContentValues()
            values.put("album_id", albumId)
            values.put("_data", path)

            contentResolver.insert(artworkUri, values)
        }


        private fun createAlbumArtDir(): File {
            val albumArtDir = File(Environment.getExternalStorageDirectory(), "/albumthumbs/")
            if (!albumArtDir.exists()) {
                albumArtDir.mkdirs()
                try {
                    File(albumArtDir, ".nomedia").createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return albumArtDir
        }

        fun deleteAlbumArt(context: Context, albumId: Int) {
            val contentResolver = context.contentResolver
            val localUri = Uri.parse("content://media/external/audio/albumart")
            contentResolver.delete(ContentUris.withAppendedId(localUri, albumId.toLong()), null, null)
        }

        fun createAlbumArtFile(): File {
            return File(createAlbumArtDir(), System.currentTimeMillis().toString())
        }

        fun getMediaStoreAlbumCoverUri(albumId: Int): Uri {
            val sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart")

            return ContentUris.withAppendedId(sArtworkUri, albumId.toLong())
        }

        val TAG = MusicUtil::class.java.simpleName

        fun getSongFileUri(songId: Int): Uri {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId.toLong())
        }

        fun getReadableDurationString(songDurationMillis: Long): String {
            var minutes = songDurationMillis / 1000 / 60
            val seconds = songDurationMillis / 1000 % 60
            if (minutes < 60) {
                return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds)
            } else {
                val hours = minutes / 60
                minutes = minutes % 60
                return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            }
        }

        //iTunes uses for example 1002 for track 2 CD1 or 3011 for track 11 CD3.
        //this method converts those values to normal tracknumbers
        fun getFixedTrackNumber(trackNumberToFix: Int): Int {
            return trackNumberToFix % 1000
        }


        fun getLyrics(song: Song): String? {
            var lyrics: String? = null

            val file = File(song.data)

            try {
                lyrics = AudioFileIO.read(file).tagOrCreateDefault.getFirst(FieldKey.LYRICS)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (lyrics == null || lyrics.trim { it <= ' ' }.isEmpty()) {
                val dir = file.absoluteFile.parentFile

                if (dir != null && dir.exists() && dir.isDirectory) {
                    val format = ".*%s.*\\.(lrc|txt)"
                    val filename = Pattern.quote(FileUtil.stripExtension(file.name))
                    val songtitle = Pattern.quote(song.title)

                    val patterns = ArrayList<Pattern>()
                    patterns.add(Pattern.compile(String.format(format, filename), Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE))
                    patterns.add(Pattern.compile(String.format(format, songtitle), Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE))


                    val files: Array<out File>? = dir.listFiles(FileFilter {
                        for (pattern in patterns) {
                            if (pattern.matcher(it.name).matches()) return@FileFilter true
                        }
                        return@FileFilter false
                    })

                    if (files!!.isNotEmpty()) {
                        for (f in files) {
                            try {
                                val newLyrics = FileUtil.read(f)
                                if (newLyrics.isNotEmpty() && !newLyrics.trim { it <= ' ' }.isEmpty()) {
                                    lyrics = newLyrics
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }
            }
            return lyrics
        }


        fun isArtistNameUnknown(artistName: String?): Boolean {
            var artistName = artistName
            if (TextUtils.isEmpty(artistName)) return false
            artistName = artistName!!.trim { it <= ' ' }.toLowerCase()
            return artistName == "unknown" || artistName == "<unknown>"
        }
    }
}
