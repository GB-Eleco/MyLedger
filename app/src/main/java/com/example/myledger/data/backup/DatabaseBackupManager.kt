package com.example.myledger.data.backup

import android.content.Context
import android.net.Uri
import com.example.myledger.data.database.AppDatabase
import java.io.FileOutputStream

object DatabaseBackupManager {
    private const val DATABASE_NAME = "my_ledger_database"

    fun export(context: Context, uri: Uri) {
        val database = AppDatabase.getDatabase(context)
        database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
        context.contentResolver.openOutputStream(uri)?.use { output ->
            context.getDatabasePath(DATABASE_NAME).inputStream().use { it.copyTo(output) }
        }
    }

    fun restore(context: Context, uri: Uri) {
        AppDatabase.closeDatabase()
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(databaseFile, false).use { input.copyTo(it) }
        }
        FileOutputStream("${databaseFile.path}-wal", false).close()
        FileOutputStream("${databaseFile.path}-shm", false).close()
    }
}
