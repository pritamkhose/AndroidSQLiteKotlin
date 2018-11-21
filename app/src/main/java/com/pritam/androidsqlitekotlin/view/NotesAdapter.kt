package com.pritam.androidsqlitekotlin.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pritam.androidsqlitekotlin.R
import com.pritam.androidsqlitekotlin.sqlite.database.model.Note
import java.text.ParseException
import java.text.SimpleDateFormat


class NotesAdapter(private val context: Context, private val notesList: List<Note>) : RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var note: TextView
        var dot: TextView
        var timestamp: TextView

        init {
            note = view.findViewById(R.id.note)
            dot = view.findViewById(R.id.dot)
            timestamp = view.findViewById(R.id.timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.note_list_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val note = notesList[position]

        holder.note.setText(note.note.toString())

        // Displaying dot from HTML character code
        holder.dot.text = Html.fromHtml("&#8226;")

        // Formatting and displaying timestamp
        holder.timestamp.text = formatDate(note.timestamp.toString())
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private fun formatDate(dateStr: String): String {
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = fmt.parse(dateStr)
            val fmtOut = SimpleDateFormat("MMM d")
            return fmtOut.format(date)
        } catch (e: ParseException) {

        }

        return ""
    }
}

