package com.example.smartzone.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.smartzone.R
import com.example.smartzone.entities.Zone
import com.example.smartzone.helpers.ZonesHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartzone.adapters.DocumentAdapter
import com.example.smartzone.adapters.NoteAdapter
import com.example.smartzone.entities.Note
import com.example.smartzone.helpers.DocumentsHelper
import com.example.smartzone.helpers.NotesHelper


class ZoneDetailActivity : AppCompatActivity() {

    private lateinit var zoneId: String
    private lateinit var zone: Zone
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private var notes: MutableList<Note> = mutableListOf()
    private lateinit var documentAdapter: DocumentAdapter
    private lateinit var documentsRecyclerView: RecyclerView
    private lateinit var pdfLauncher: ActivityResultLauncher<Intent>

    private val zonesHelper = ZonesHelper()
    private val notesHelper = NotesHelper()
    private val documentsHelper = DocumentsHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        Log.e("PDF_PERMISSION", "Persistable URI permission failed", e)
                    }

                    val name = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIndex != -1) {
                            cursor.getString(nameIndex)
                        } else {
                            "document.pdf"
                        }
                    } ?: "document.pdf"

                    documentsHelper.addDocument(zoneId, name, uri.toString(), {
                        Toast.makeText(this, "Document added", Toast.LENGTH_SHORT).show()
                        loadDocuments()
                    }, {
                        Toast.makeText(this, "Failed to add document", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }

        setContentView(R.layout.activity_zone_detail)

        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val titleText = findViewById<TextView>(R.id.zoneTitleText)
        val focusText = findViewById<TextView>(R.id.zoneFocusText)
        val createdText = findViewById<TextView>(R.id.zoneCreatedDateDetail)
        val editButton = findViewById<ImageButton>(R.id.editZoneButton)
        val deleteButton = findViewById<ImageButton>(R.id.deleteZoneButton)

        zoneId = intent.getStringExtra("zoneId") ?: return finish()

        zonesHelper.getZoneById(zoneId, { loadedZone ->
            zone = loadedZone
            titleText.text = zone.name
            focusText.text = "Focus: ${zone.focus}"
            createdText.text = "Created on: ${formatDate(zone.createdAt.toDate().time)}"
        }, {
            Toast.makeText(this, "Error loading zone", Toast.LENGTH_SHORT).show()
            finish()
        })

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(this, notes, zoneId) {
            loadNotes()
        }

        notesRecyclerView.adapter = noteAdapter

        loadNotes()
        documentsRecyclerView = findViewById(R.id.documentsRecyclerView)
        documentsRecyclerView.layoutManager = LinearLayoutManager(this)
        documentAdapter = DocumentAdapter(this, zoneId) {
            loadDocuments()
        }
        documentsRecyclerView.adapter = documentAdapter

        loadDocuments()

        val addDocumentButton = findViewById<Button>(R.id.addDocumentButton)
        addDocumentButton.setOnClickListener {
            pickPdfDocument()
        }

        editButton.setOnClickListener {
            showEditDialog()
        }

        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Zone")
                .setMessage("Are you sure you want to delete this zone? All notes inside will also be lost.")
                .setPositiveButton("Delete") { _, _ ->
                    zonesHelper.deleteZone(zoneId, {
                        Toast.makeText(this, "Zone deleted", Toast.LENGTH_SHORT).show()
                        finish()
                        val intent = Intent(this, ZonesActivity::class.java)
                        startActivity(intent)
                    }, {
                        Toast.makeText(this, "Failed to delete zone", Toast.LENGTH_SHORT).show()
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val addNoteButton = findViewById<Button>(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            showAddNoteDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_header, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_zone, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editZoneNameEditText)
        val focusEditText = dialogView.findViewById<EditText>(R.id.editZoneFocusEditText)
        val saveButton = dialogView.findViewById<TextView>(R.id.saveEditButton)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelEditButton)

        nameEditText.setText(zone.name)
        focusEditText.setText(zone.focus)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        saveButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            val newFocus = focusEditText.text.toString().trim()

            if (newName.isNotEmpty()) {
                zonesHelper.updateZone(zoneId, newName, newFocus, {
                    Toast.makeText(this, "Zone updated", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    finish()
                    startActivity(intent)
                }, {
                    Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
                })
            } else {
                nameEditText.error = "Name can't be empty"
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun loadNotes() {
        notesHelper.getNotes(zoneId, { loadedNotes ->
            notes.clear()
            notes.addAll(loadedNotes)
            noteAdapter.updateNotes(notes)
        }, {
            Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show()
        })
    }

    private fun showAddNoteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.noteTitleEditText)
        val contentEdit = dialogView.findViewById<EditText>(R.id.noteContentEditText)
        val saveButton = dialogView.findViewById<TextView>(R.id.saveNoteButton)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelNoteButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        saveButton.setOnClickListener {
            val title = titleEdit.text.toString().trim()
            val content = contentEdit.text.toString().trim()

            if (title.isEmpty()) {
                titleEdit.error = "Title required"
                return@setOnClickListener
            }

            notesHelper.addNote(zoneId, title, content, {
                Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadNotes()
            }, {
                Toast.makeText(this, "Failed to add note", Toast.LENGTH_SHORT).show()
            })
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadDocuments() {
        documentsHelper.getDocuments(zoneId, { loadedDocs ->
            Log.d("ZoneDetailActivity", "Loaded documents count: ${loadedDocs.size}")
            documentAdapter.updateDocuments(loadedDocs)
        }, {
            Toast.makeText(this, "Failed to load documents", Toast.LENGTH_SHORT).show()
        })
    }

    private fun pickPdfDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        pdfLauncher.launch(intent)
    }
}
