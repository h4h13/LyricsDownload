package code.name.monkey.companion.util

import android.util.Base64
import java.io.*
import java.nio.charset.Charset

/**
 * Created by hefuyi on 2016/11/8.
 */

object LyricUtil {

    private val lrcRootPath = android.os.Environment
            .getExternalStorageDirectory().toString() + "/RetroMusic/lyrics/"

    fun writeLrcToLoc(title: String, artist: String, lrcContext: String): File? {
        var writer: FileWriter? = null
        try {
            val file = File(getLrcPath(title, artist))
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            writer = FileWriter(getLrcPath(title, artist))
            writer.write(lrcContext)
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                if (writer != null)
                    writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun deleteLrcFile(title: String, artist: String): Boolean {
        val file = File(getLrcPath(title, artist))
        return file.delete()
    }

    fun isLrcFileExist(title: String, artist: String): Boolean {
        val file = File(getLrcPath(title, artist))
        return file.exists()
    }

    fun getLocalLyricFile(title: String, artist: String): File {
        val file = File(getLrcPath(title, artist))
        return if (file.exists()) {
            file
        } else {
            File("lyric file not exist")
        }
    }

    private fun getLrcPath(title: String, artist: String): String {
        return "$lrcRootPath$title - $artist.lrc"
    }

    fun decryptBASE64(str: String?): String? {
        if (str == null || str.length == 0) {
            return null
        }
        try {
            val encode = str.toByteArray(charset("UTF-8"))
            // base64 解密
            return String(Base64.decode(encode, 0, encode.size, Base64.DEFAULT), Charset.forName("UTF-8"))

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(Exception::class)
    fun getStringFromFile(title: String, artist: String): String {
        val file = File(getLrcPath(title, artist))
        val fin = FileInputStream(file)
        val ret = convertStreamToString(fin)
        fin.close()
        return ret
    }

    @Throws(Exception::class)
    private fun convertStreamToString(`is`: InputStream): String {
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()
        var line: String
        while (reader.readLine() != null) {
            line = reader.readLine()
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }
}
