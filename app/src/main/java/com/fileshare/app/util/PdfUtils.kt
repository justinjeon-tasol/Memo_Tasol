package com.fileshare.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    
    /**
     * PDF 첫 페이지를 Bitmap으로 렌더링
     */
    fun renderFirstPage(context: Context, pdfFile: File, width: Int = 400): Bitmap? {
        return try {
            if (!pdfFile.exists()) return null
            
            val fileDescriptor = ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            val pdfRenderer = PdfRenderer(fileDescriptor)
            
            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0)
                
                // Calculate height maintaining aspect ratio
                val aspectRatio = page.height.toFloat() / page.width.toFloat()
                val height = (width * aspectRatio).toInt()
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                // 흰색 배경으로 초기화 (투명 배경 방지)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                page.close()
                pdfRenderer.close()
                fileDescriptor.close()
                
                bitmap
            } else {
                pdfRenderer.close()
                fileDescriptor.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * PDF 페이지 수 확인
     */
    fun getPageCount(pdfFile: File): Int {
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount
            
            pdfRenderer.close()
            fileDescriptor.close()
            
            pageCount
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * PDF인지 확인
     */
    fun isPdf(uri: Uri, context: Context): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType == "application/pdf" || uri.toString().endsWith(".pdf", ignoreCase = true)
    }
    
    /**
     * PDF 파일을 내부 저장소로 복사
     */
    fun copyPdfToInternalStorage(context: Context, sourceUri: Uri, fileName: String): String? {
        return try {
            // FileUtils와 동일하게 documents 폴더 사용
            val documentsDir = File(context.filesDir, "documents")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            
            val destFile = File(documentsDir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
