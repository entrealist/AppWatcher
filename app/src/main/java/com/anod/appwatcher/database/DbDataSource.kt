package com.anod.appwatcher.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper

/**
 * @author Alex Gavrishev
 * @date 21/05/2018
 */
class DbDataSource(private val db: SupportSQLiteOpenHelper) : SupportSQLiteOpenHelper {
    override fun getDatabaseName(): String? {
        return db.databaseName
    }

    override fun getWritableDatabase(): SupportSQLiteDatabase {
        return db.writableDatabase
    }

    override fun getReadableDatabase(): SupportSQLiteDatabase {
        return db.readableDatabase
    }

    override fun close() {
        db.close()
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        db.setWriteAheadLoggingEnabled(enabled)
    }
}