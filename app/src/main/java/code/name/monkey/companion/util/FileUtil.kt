package code.name.monkey.companion.util

import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import code.name.monkey.companion.SongLoader
import code.name.monkey.companion.SortedCursor
import code.name.monkey.companion.mvp.model.Song
import io.reactivex.Observable
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object FileUtil {

    fun matchFilesWithMediaStore(context: Context, files: List<File>): Observable<ArrayList<Song>> {
        return SongLoader.getSongs(makeSongCursor(context, files))
    }

    fun makeSongCursor(context: Context, files: List<File>): SortedCursor? {
        var selection: String? = null
        val paths = toPathArray(files).toTypedArray()

        if (files.size in 1..998) { // 999 is the max amount Androids SQL implementation can handle.
            selection = MediaStore.Audio.AudioColumns.DATA + " IN (" + makePlaceholders(files.size) + ")"
        }

        val songCursor = SongLoader.makeSongCursor(context, selection, if (selection == null) null else paths)

        return if (songCursor == null) null else SortedCursor(songCursor, paths, MediaStore.Audio.AudioColumns.DATA)
    }

    private fun makePlaceholders(len: Int): String {
        val sb = StringBuilder(len * 2 - 1)
        sb.append("?")
        for (i in 1 until len) {
            sb.append(",?")
        }
        return sb.toString()
    }

    private fun toPathArray(files: List<File>): ArrayList<String> {
        val paths = ArrayList<String>()
        if (files.isNotEmpty()) {
            for (file in files) {
                paths.add(safeGetCanonicalPath(file))
            }
        }
        return paths
    }

    fun listFiles(directory: File, fileFilter: FileFilter?): List<File> {
        val fileList = LinkedList<File>()
        val found = directory.listFiles(fileFilter)
        if (found != null) {
            Collections.addAll(fileList, *found)
        }
        return fileList
    }

    fun listFilesDeep(directory: File, fileFilter: FileFilter?): List<File> {
        val files = LinkedList<File>()
        internalListFilesDeep(files, directory, fileFilter)
        return files
    }

    fun listFilesDeep(files: Collection<File>, fileFilter: FileFilter?): List<File> {
        val resFiles = LinkedList<File>()
        for (file in files) {
            if (file.isDirectory) {
                internalListFilesDeep(resFiles, file, fileFilter)
            } else if (fileFilter == null || fileFilter.accept(file)) {
                resFiles.add(file)
            }
        }
        return resFiles
    }

    private fun internalListFilesDeep(files: MutableCollection<File>, directory: File, fileFilter: FileFilter?) {
        val found = directory.listFiles(fileFilter)

        if (found != null) {
            for (file in found) {
                if (file.isDirectory) {
                    internalListFilesDeep(files, file, fileFilter)
                } else {
                    files.add(file)
                }
            }
        }
    }

    fun fileIsMimeType(file: File, mimeType: String?, mimeTypeMap: MimeTypeMap): Boolean {
        if (mimeType == null || mimeType == "*/*") {
            return true
        } else {
            // get the file mime type
            val filename = file.toURI().toString()
            val dotPos = filename.lastIndexOf('.')
            if (dotPos == -1) {
                return false
            }
            val fileExtension = filename.substring(dotPos + 1).toLowerCase()
            val fileType = mimeTypeMap.getMimeTypeFromExtension(fileExtension) ?: return false
            // check the 'type/subtype' pattern
            if (fileType == mimeType) {
                return true
            }
            // check the 'type/*' pattern
            val mimeTypeDelimiter = mimeType.lastIndexOf('/')
            if (mimeTypeDelimiter == -1) {
                return false
            }
            val mimeTypeMainType = mimeType.substring(0, mimeTypeDelimiter)
            val mimeTypeSubtype = mimeType.substring(mimeTypeDelimiter + 1)
            if (mimeTypeSubtype != "*") {
                return false
            }
            val fileTypeDelimiter = fileType.lastIndexOf('/')
            if (fileTypeDelimiter == -1) {
                return false
            }
            val fileTypeMainType = fileType.substring(0, fileTypeDelimiter)
            if (fileTypeMainType == mimeTypeMainType) {
                return true
            }
        }
        return false
    }

    fun stripExtension(str: String?): String? {
        if (str == null) return null
        val pos = str.lastIndexOf('.')
        return if (pos == -1) str else str.substring(0, pos)
    }

    fun readFromStream(`is`: InputStream): String {
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()
        var line: String
        while (reader.readLine() != null) {
            line = reader.readLine()
            if (sb.isNotEmpty()) sb.append("\n")
            sb.append(line)
        }
        reader.close()
        return sb.toString()
    }

    fun read(file: File): String {
        val fin = FileInputStream(file)
        val ret = readFromStream(fin)
        fin.close()
        return ret
    }

    fun safeGetCanonicalPath(file: File): String {
        return try {
            file.canonicalPath
        } catch (e: IOException) {
            e.printStackTrace()
            file.absolutePath
        }

    }
}
