package edu.illinois.cs.cs124.ay2023.mp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2023.mp.R
import edu.illinois.cs.cs124.ay2023.mp.models.Summary

/**
 * Adapter to display a list of summaries using Android's RecyclerView.
 *
 * <p>You should not need to modify this code, although you may want to.
 *
 * @noinspection unused
 */
class SummaryListAdapter(
    summaries: List<Summary>,
    private val activity: Activity,
    private val onClickCallback: ((summary: Summary) -> Any?)? = null,
) : RecyclerView.Adapter<SummaryListAdapter.ViewHolder>() {
    var summaries: List<Summary> = summaries
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            activity.runOnUiThread { notifyDataSetChanged() }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryListAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryListAdapter.ViewHolder, position: Int) {
        val summary = summaries[position]
        // Set the title text as the result of calling toString
        holder.title.text = summary.toString()
        holder.layout.setOnClickListener { onClickCallback?.invoke(summary) }
    }

    override fun getItemCount() = summaries.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout: LinearLayout
        val title: TextView

        init {
            layout = itemView.findViewById(R.id.layout)
            title = itemView.findViewById(R.id.title)
        }
    }
}
