package code.name.monkey.companion.mvp.model

import android.os.Parcel
import android.os.Parcelable


/**
 * @author Hemanth S (h4h13).
 */
data class Song(val id: Int,
                val title: String,
                val trackNumber: Int,
                val year: Int,
                val duration: Long,
                val data: String,
                val dateModified: Long,
                val albumId: Int,
                val albumName: String,
                val artistId: Int,
                val artistName: String) : Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);
        dest.writeString(this.data);
        dest.writeLong(this.dateModified);
        dest.writeInt(this.albumId);
        dest.writeString(this.albumName);
        dest.writeInt(this.artistId);
        dest.writeString(this.artistName);
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {

        val EMPTY_SONG = Song(-1, "", -1, -1, -1, "", -1, -1, "", -1, "")

        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}