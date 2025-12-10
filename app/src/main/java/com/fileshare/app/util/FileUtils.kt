package com.fileshare.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    
    private const val DOCUMENTS_DIR = "documents"
    private const val AUTHORITY_SUFFIX = ".fileprovider"
    
    /**
     * Save file from URI to app's internal storage
     */
    fun saveFileToInternalStorage(
        context: Context,
        sourceUri: Uri,
        fileName: String
    ): String? {
        return try {
            val documentsDir = File(context.filesDir, DOCUMENTS_DIR)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            
            val destinationFile = File(documentsDir, fileName)
            
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            destinationFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Save bitmap to internal storage
     */
    fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): String? {
        return try {
            val documentsDir = File(context.filesDir, DOCUMENTS_DIR)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            
            val file = File(documentsDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Delete file from internal storage
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get file URI for sharing via FileProvider
     */
    fun getFileUri(context: Context, filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}$AUTHORITY_SUFFIX",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Share single file
     */
    fun shareFile(context: Context, filePath: String, mimeType: String = "image/*") {
        val uri = getFileUri(context, filePath) ?: return
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "문서 공유하기"))
    }
    
    /**
     * Share multiple files
     */
    fun shareMultipleFiles(
        context: Context,
        filePaths: List<String>,
        mimeType: String = "image/*"
    ) {
        val uris = ArrayList<Uri>()
        filePaths.forEach { path ->
            getFileUri(context, path)?.let { uris.add(it) }
        }
        
        if (uris.isEmpty()) return
        
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "문서 공유하기"))
    }
    
    /**
     * Get file size in bytes
     */
    fun getFileSize(filePath: String): Long {
        return try {
            File(filePath).length()
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Check if file exists
     */
    fun fileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate unique file name
     */
    fun generateFileName(prefix: String = "doc", extension: String = "jpg"): String {
        val timestamp = System.currentTimeMillis()
        return "${prefix}_${timestamp}.$extension"
    }
    
    /**
     * Get MIME type from file extension
     */
    fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    /**
     * Open file with external viewer
     */
    fun openFile(context: Context, filePath: String) {
        val uri = getFileUri(context, filePath) ?: run {
            android.widget.Toast.makeText(context, "파일을 찾을 수 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        val mimeType = getMimeType(filePath)
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "파일을 열 수 있는 앱이 설치되어 있지 않습니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
