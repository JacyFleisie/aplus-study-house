package com.aplusstudyhouse.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.aplusstudyhouse.app.R
import com.aplusstudyhouse.app.data.MockInvoice
import com.aplusstudyhouse.app.data.MockPayment
import com.aplusstudyhouse.app.data.MockStudent
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Small, dependency-free financial documents for download/share from the app. */
object FinancePdf {
    fun invoice(context: Context, invoice: MockInvoice, student: MockStudent?): Uri =
        createDocument(
            context = context,
            title = if (invoice.status.name == "PAID") "Payment Receipt" else "Fee Invoice",
            filePrefix = if (invoice.status.name == "PAID") "receipt" else "invoice",
            fields = listOf(
                "Invoice reference" to invoice.reference.ifBlank { invoice.id },
                "Student" to (student?.let { "${it.firstName} ${it.lastName}" } ?: invoice.studentName.ifBlank { "—" }),
                "Description" to invoice.description,
                "Category" to invoice.category.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                "Amount" to "R${"%.2f".format(Locale.US, invoice.amount)}",
                "Due date" to invoice.dueDate.ifBlank { "—" },
                "Status" to invoice.status.name.lowercase().replaceFirstChar { it.uppercase() },
                "Paid date" to (invoice.paidDate ?: "—")
            )
        )

    fun receipt(context: Context, payment: MockPayment, invoice: MockInvoice?, student: MockStudent?): Uri =
        createDocument(
            context = context,
            title = "Payment Receipt",
            filePrefix = "receipt",
            fields = listOf(
                "Receipt reference" to payment.reference,
                "Invoice reference" to (invoice?.reference?.ifBlank { payment.invoiceId } ?: payment.invoiceId),
                "Student" to (student?.let { "${it.firstName} ${it.lastName}" } ?: payment.studentName.ifBlank { "—" }),
                "Payment date" to payment.paymentDate,
                "Payment method" to payment.paymentMethod.name,
                "Amount" to "R${"%.2f".format(Locale.US, payment.amount)}",
                "Status" to payment.status.name.lowercase().replaceFirstChar { it.uppercase() }
            )
        )

    fun share(context: Context, uri: Uri, subject: String = "A+ Study House financial document") {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share PDF"
            )
        )
    }

    private fun createDocument(
        context: Context,
        title: String,
        filePrefix: String,
        fields: List<Pair<String, String>>
    ): Uri {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 25f; isFakeBoldText = true; color = 0xff17324d.toInt() }
        val subheading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = 0xff527087.toInt() }
        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f; isFakeBoldText = true; color = 0xff17324d.toInt() }
        val value = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = 0xff263238.toInt() }
        val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xffd7e2ea.toInt(); strokeWidth = 1f }

        BitmapFactory.decodeResource(context.resources, R.drawable.logo_full)?.let {
            val scale = minOf(150f / it.width, 55f / it.height)
            canvas.drawBitmap(it, null, android.graphics.RectF(42f, 38f, 42f + it.width * scale, 38f + it.height * scale), null)
            it.recycle()
        }
        canvas.drawText(title, 42f, 145f, heading)
        canvas.drawText("A+ Study House  •  Official school document", 42f, 166f, subheading)
        canvas.drawText(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()), 440f, 166f, subheading)
        canvas.drawLine(42f, 185f, 553f, 185f, rule)

        var y = 220f
        fields.forEach { (field, fieldValue) ->
            canvas.drawText(field,  fifty(), y, label)
            canvas.drawText(fieldValue.take(68), 235f, y, value)
            canvas.drawLine(42f, y + 13f, 553f, y + 13f, rule)
            y += 42f
        }
        y += 22f
        canvas.drawText("Payment instructions", 42f, y, label)
        y += 22f
        canvas.drawText("Pay securely through the A+ Study House app or contact the office.", 42f, y, value)
        y += 20f
        canvas.drawText("This PDF is generated from the school's recorded finance data.", 42f, y, subheading)
        document.finishPage(page)

        val directory = File(context.cacheDir, "finance-documents").apply { mkdirs() }
        val file = File(directory, "$filePrefix-${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun fifty(): Float = 42f
}
