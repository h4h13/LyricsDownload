package code.name.monkey.companion.lyrics

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import code.name.monkey.companion.R
import code.name.monkey.companion.mvp.model.LoadingInfo
import code.name.monkey.companion.util.MusicUtil
import com.afollestad.materialdialogs.MaterialDialog
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.CannotWriteException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.TagException
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class WriteTagsAsyncTask(applicationContext: Context) :
        DialogAsyncTask<LoadingInfo, Int, Array<String>>(applicationContext) {

    override fun doInBackground(vararg params: LoadingInfo): Array<String>? {
        try {
            val info = params[0]

            var artwork: Artwork? = null
            var albumArtFile: File? = null
            if (info.artworkInfo?.artwork != null) {
                try {
                    albumArtFile = MusicUtil.createAlbumArtFile().canonicalFile
                    info.artworkInfo.artwork
                            .compress(Bitmap.CompressFormat.PNG, 0, FileOutputStream(albumArtFile!!))
                    artwork = ArtworkFactory.createArtworkFromFile(albumArtFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            var counter = 0
            var wroteArtwork = false
            var deletedArtwork = false
            for (filePath in info.filePaths) {
                publishProgress(++counter, info.filePaths.size)
                try {
                    val audioFile = AudioFileIO.read(File(filePath))
                    val tag = audioFile.tagOrCreateAndSetDefault

                    for ((key, value) in info.fieldKeyValueMap) {
                        try {
                            tag.setField(key, value)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (info.artworkInfo != null) {
                        if (info.artworkInfo.artwork == null) {
                            tag.deleteArtworkField()
                            deletedArtwork = true
                        } else if (artwork != null) {
                            tag.deleteArtworkField()
                            tag.setField(artwork)
                            wroteArtwork = true
                        }
                    }
                    audioFile.commit()
                } catch (e: CannotReadException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: CannotWriteException) {
                    e.printStackTrace()
                } catch (e: TagException) {
                    e.printStackTrace()
                } catch (e: ReadOnlyFileException) {
                    e.printStackTrace()
                } catch (e: InvalidAudioFrameException) {
                    e.printStackTrace()
                }

            }

            val context = context
            if (context != null) {
                if (wroteArtwork) {
                    MusicUtil.insertAlbumArt(context, info.artworkInfo?.albumId!!, albumArtFile!!.path)
                } else if (deletedArtwork) {
                    MusicUtil.deleteAlbumArt(context, info.artworkInfo?.albumId!!)
                }
            }

            return info.filePaths.toTypedArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    override fun onPostExecute(toBeScanned: Array<String>) {
        super.onPostExecute(toBeScanned)
        scan(toBeScanned)
    }

    override fun onCancelled(toBeScanned: Array<String>) {
        super.onCancelled(toBeScanned)
        scan(toBeScanned)
    }

    private fun scan(toBeScanned: Array<String>) {
        val context = context
        Toast.makeText(context, "Done", Toast.LENGTH_LONG).show()
        //MediaScannerConnection.scanFile(applicationContext, toBeScanned, null, if (context is Activity) UpdateToastMediaScannerCompletionListener(context as Activity?, toBeScanned) else null)
    }

    override fun createDialog(context: Context): Dialog {
        return MaterialDialog.Builder(context)
                .title(R.string.saving_changes)
                .cancelable(false)
                .progress(false, 0)
                .build()
    }
}