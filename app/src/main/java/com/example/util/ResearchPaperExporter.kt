package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.ResearchPaperDraft
import java.io.OutputStreamWriter

object ResearchPaperExporter {

    fun copyToClipboard(context: Context, text: String, label: String = "Academic Research Draft") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareAsPlainText(context: Context, draft: ResearchPaperDraft) {
        val text = draft.toFormattedPlainText()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Research Paper Draft: ${draft.academicTitle}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share Research Draft via")
        context.startActivity(chooser)
    }

    fun writeDraftToUri(context: Context, uri: Uri, draft: ResearchPaperDraft): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(draft.toFormattedPlainText())
                }
            }
            Toast.makeText(context, "Saved research draft successfully!", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
