package code.name.monkey.companion

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import code.name.monkey.companion.lyrics.KogouLyricsFetcher
import code.name.monkey.companion.lyrics.SongDetailsActivity
import code.name.monkey.companion.mvp.model.Song

/**
 * @author Hemanth S (h4h13).
 */
class SongsAdapter(var context: Context) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {
    var dataSet = ArrayList<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list, parent, false))
    }

    fun swapData(result: ArrayList<Song>) {
        dataSet = result
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView)  {
        /*override fun onNoLyrics() {
            println("Lyrics No")
            TransitionManager.beginDelayedTransition(root)
        }

        override fun onLyrics() {
            TransitionManager.beginDelayedTransition(root)
            title.setTextColor(Color.GREEN)
            text.setTextColor(Color.GREEN)
        }*/


        var title: AppCompatTextView = itemView.findViewById(R.id.title)
        var text: AppCompatTextView = itemView.findViewById(R.id.text)
        var root: ViewGroup = itemView.findViewById(R.id.root)

        fun bind(song: Song) {

            title.text = song.title
            text.text = song.artistName

            itemView.setOnClickListener({
                TransitionManager.beginDelayedTransition(root)
                ContextCompat.startActivity(context, Intent(context, SongDetailsActivity::class.java)
                        .putExtra("song", song), null)
            })
        }
    }
}