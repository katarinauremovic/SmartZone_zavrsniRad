package com.example.smartzone.activities

import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartzone.R
import com.example.smartzone.adapters.PlannerAdapter
import com.example.smartzone.databinding.ActivityPlannerBinding
import com.example.smartzone.helpers.PlannerHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.ZoneId

class PlannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlannerBinding
    private lateinit var adapter: PlannerAdapter
    private lateinit var helper: PlannerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        helper = PlannerHelper(this, FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

        setupRecyclerView()
        observeEvents()

        binding.fabAddEvent.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = PlannerAdapter { event ->
            event.id?.let {
                lifecycleScope.launch {
                    helper.deleteEvent(it)
                }
            }
        }

        binding.rvPlanner.layoutManager = LinearLayoutManager(this)
        binding.rvPlanner.adapter = adapter
    }

    private fun observeEvents() {
        helper.observeEventsDirect { events ->
            adapter.submitList(events.sortedBy { it.weekday * 1440 + it.startMinutes })
        }
    }



    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_planner_event, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val etReminder = dialogView.findViewById<EditText>(R.id.etReminder)

        timePicker.setIs24HourView(true)

        val daysAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.days_of_week,
            android.R.layout.simple_spinner_item
        )
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = daysAdapter

        AlertDialog.Builder(this)
            .setTitle("Add Planner Event")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val dayIndex = spinnerDay.selectedItemPosition + 1
                val hour = if (Build.VERSION.SDK_INT >= 23) timePicker.hour else timePicker.currentHour
                val minute = if (Build.VERSION.SDK_INT >= 23) timePicker.minute else timePicker.currentMinute
                val reminder = etReminder.text.toString().toIntOrNull() ?: 10

                lifecycleScope.launch {
                    helper.addEventFromDialog(
                        title = title,
                        weekday = dayIndex,
                        hour = hour,
                        minute = minute,
                        reminder = reminder
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
