package com.example.smartzone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartzone.R
import com.example.smartzone.entities.Zone
import java.text.SimpleDateFormat
import java.util.*

class ZoneAdapter(
    private var zones: List<Zone>,
    private val onZoneClick: (Zone) -> Unit
) : RecyclerView.Adapter<ZoneAdapter.ZoneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_zone, parent, false)
        return ZoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.bind(zones[position])
    }

    override fun getItemCount(): Int = zones.size

    fun updateList(newZones: List<Zone>) {
        zones = newZones
        notifyDataSetChanged()
    }

    inner class ZoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val zoneName: TextView = itemView.findViewById(R.id.zoneNameText)
        private val zoneFocus: TextView = itemView.findViewById(R.id.zoneFocusText)
        private val zoneDate: TextView = itemView.findViewById(R.id.zoneCreatedDateText)

        fun bind(zone: Zone) {
            zoneName.text = zone.name
            zoneFocus.text = zone.focus
            zoneDate.text = formatDate(zone.createdAt.toDate().time)

            itemView.setOnClickListener {
                onZoneClick(zone)
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
