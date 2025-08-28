package com.example.smartzone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartzone.R
import com.example.smartzone.entities.PlannerEvent
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

class PlannerAdapter( private val onDeleteClick: (PlannerEvent) -> Unit) : ListAdapter<PlannerEvent, PlannerAdapter.PlannerViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_planner_event, parent, false)
        return PlannerViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: PlannerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlannerViewHolder(itemView: View, private val onDeleteClick: (PlannerEvent) -> Unit) : RecyclerView.ViewHolder(itemView) {

        private val tvEventTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        private val tvEventTime: TextView = itemView.findViewById(R.id.tvEventTime)
        private val tvReminder: TextView = itemView.findViewById(R.id.tvReminder)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteEvent)


        fun bind(event: PlannerEvent) {
            tvEventTitle.text = event.title

            val dayOfWeek = DayOfWeek.of(event.weekday).getDisplayName(TextStyle.FULL, Locale.getDefault())
            val hour = event.startMinutes / 60
            val minute = event.startMinutes % 60
            val timeFormatted = String.format("%02d:%02d", hour, minute)

            tvEventTime.text = "$dayOfWeek, $timeFormatted"
            tvReminder.text = "Reminder: ${event.reminderMinutesBefore ?: 10} min before"
            btnDelete.setOnClickListener {
                onDeleteClick(event)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PlannerEvent>() {
        override fun areItemsTheSame(oldItem: PlannerEvent, newItem: PlannerEvent): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PlannerEvent, newItem: PlannerEvent): Boolean =
            oldItem == newItem
    }
}
