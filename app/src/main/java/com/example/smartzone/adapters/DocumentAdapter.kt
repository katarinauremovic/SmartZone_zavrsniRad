package com.example.smartzone.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.smartzone.R
import com.example.smartzone.entities.Document
import com.example.smartzone.helpers.DocumentsHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DocumentAdapter(
    private val context: Context,
    private val zoneId: String,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    private val documents = mutableListOf<Document>()
    private val documentsHelper = DocumentsHelper()

    class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val docName: TextView = view.findViewById(R.id.documentNameText)
        val docDate: TextView = view.findViewById(R.id.documentDateText)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteDocumentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        Log.d("DocumentAdapter", "onCreateViewHolder called")
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val doc = documents[position]
        Log.d("DocumentAdapter", "Binding document: ${doc.name}")

        holder.docName.text = doc.name
        holder.docDate.text = formatDate(doc.uploadedAt.toDate().time)

        holder.itemView.setOnClickListener {
            try {
                val originalUri = Uri.parse(doc.fileUri)

                val inputStream = context.contentResolver.openInputStream(originalUri)
                val file = File(context.cacheDir, "${doc.name}.pdf")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Open PDF with...")
                context.startActivity(chooser)

            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open PDF", Toast.LENGTH_SHORT).show()
                Log.e("PDF_OPEN", "Error: ${e.message}", e)
            }
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Document")
                .setMessage("Are you sure you want to delete this document?")
                .setPositiveButton("Yes") { _, _ ->
                    documentsHelper.deleteDocument(zoneId, doc.id, {
                        Toast.makeText(context, "Document deleted", Toast.LENGTH_SHORT).show()
                        onDataChanged()
                    }, {
                        Toast.makeText(context, "Error deleting document", Toast.LENGTH_SHORT).show()
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount(): Int {
        Log.d("DocumentAdapter", "getItemCount = ${documents.size}")
        return documents.size
    }

    fun updateDocuments(newList: List<Document>) {
        Log.d("DocumentAdapter", "updateDocuments called with ${newList.size} documents")
        documents.clear()
        documents.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
