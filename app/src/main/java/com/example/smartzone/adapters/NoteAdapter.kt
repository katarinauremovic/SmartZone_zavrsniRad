package com.example.smartzone.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartzone.R
import com.example.smartzone.entities.Note
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.example.smartzone.helpers.NotesHelper

class NoteAdapter(
    private val context: Context,
    private var notes: List<Note>,
    private val zoneId: String,
    private val onNoteDeleted: () -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notesHelper = NotesHelper()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.noteTitleShort)
        val contentPreview: TextView = itemView.findViewById(R.id.noteContentPreview)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteNoteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleView.text = note.title
        holder.contentPreview.text =
            if (note.content.length > 80) note.content.substring(0, 80) + "..." else note.content

        holder.itemView.setOnClickListener {
            showNoteDialog(note)
        }
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete") { _, _ ->
                    notesHelper.deleteNote(zoneId, note.id, {
                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                        onNoteDeleted()
                    }, {
                        Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show()
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    private fun showNoteDialog(note: Note) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_note, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.noteTitleEditText)
        val contentEdit = dialogView.findViewById<EditText>(R.id.noteContentEditText)
        val saveButton = dialogView.findViewById<TextView>(R.id.saveNoteButton)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelNoteButton)

        titleEdit.setText(note.title)
        contentEdit.setText(note.content)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        saveButton.setOnClickListener {
            val newTitle = titleEdit.text.toString().trim()
            val newContent = contentEdit.text.toString().trim()

            if (newTitle.isEmpty()) {
                titleEdit.error = "Title required"
                return@setOnClickListener
            }

            notesHelper.updateNote(zoneId, note.id, newTitle, newContent, {
                Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onNoteDeleted()  // Refreshes the list after update
            }, {
                Toast.makeText(context, "Failed to update note", Toast.LENGTH_SHORT).show()
            })
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}
