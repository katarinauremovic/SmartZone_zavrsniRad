package com.example.smartzone.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartzone.R
import com.example.smartzone.adapters.ZoneAdapter
import com.example.smartzone.entities.Zone
import com.example.smartzone.helpers.ZonesHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class ZonesActivity : AppCompatActivity() {

    private lateinit var zonesRecyclerView: RecyclerView
    private lateinit var zoneAdapter: ZoneAdapter
    private lateinit var searchEditText: EditText
    private lateinit var sortSpinner: Spinner

    private var allZones: List<Zone> = emptyList()

    private val zonesHelper = ZonesHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        setContentView(R.layout.activity_zones)

        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        zonesRecyclerView = findViewById(R.id.zonesRecyclerView)
        searchEditText = findViewById(R.id.searchZoneEditText)
        sortSpinner = findViewById(R.id.sortZoneSpinner)
        val addZoneButton = findViewById<FloatingActionButton>(R.id.addZoneButton)
        val openPlaner = findViewById<FloatingActionButton>(R.id.fabOpenPlaner)

        openPlaner.setOnClickListener {
            startActivity(Intent(this, PlannerActivity::class.java))

        }

        zoneAdapter = ZoneAdapter(emptyList()) { selectedZone ->
            val intent = Intent(this, ZoneDetailActivity::class.java)
            intent.putExtra("zoneId", selectedZone.id)
            startActivity(intent)
        }

        zonesRecyclerView.layoutManager = LinearLayoutManager(this)
        zonesRecyclerView.adapter = zoneAdapter

        zonesHelper.loadZones(
            userId ,
            onSuccess = { zones ->
                allZones = zones
                updateDisplayedZones()
            },
            onFailure = { e ->
                Toast.makeText(this, "Error loading zones", Toast.LENGTH_SHORT).show()
            }
        )

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateDisplayedZones()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val sortOptions = listOf("Newest first", "Oldest first")
        sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                updateDisplayedZones()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        addZoneButton.setOnClickListener {
            showCreateZoneDialog()
        }
    }

    private fun updateDisplayedZones() {
        val query = searchEditText.text.toString()
        val filtered = zonesHelper.filterZones(allZones, query)
        val sorted = zonesHelper.sortZones(filtered, sortSpinner.selectedItemPosition == 0)
        zoneAdapter.updateList(sorted)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_header, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createZone(name: String, focus: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        zonesHelper.createZone(
            userId = userId,
            name = name,
            focus = focus,
            onSuccess = {
                Toast.makeText(this, "Zone created", Toast.LENGTH_SHORT).show()
                zonesHelper.loadZones(
                    userId,
                    onSuccess = { zones ->
                        allZones = zones
                        updateDisplayedZones()
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Error loading zones", Toast.LENGTH_SHORT).show()
                    })
            },
            onFailure = {
                Toast.makeText(this, "Failed to create zone", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showCreateZoneDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_zone, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val nameEditText = dialogView.findViewById<EditText>(R.id.zoneNameEditText)
        val focusEditText = dialogView.findViewById<EditText>(R.id.zoneFocusEditText)
        val cancelBtn = dialogView.findViewById<TextView>(R.id.cancelButton)
        val createBtn = dialogView.findViewById<TextView>(R.id.createButton)

        cancelBtn.setOnClickListener { dialog.dismiss() }

        createBtn.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val focus = focusEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                createZone(name, focus)
                dialog.dismiss()
            } else {
                nameEditText.error = "Required"
            }
        }

        dialog.show()
    }
}
