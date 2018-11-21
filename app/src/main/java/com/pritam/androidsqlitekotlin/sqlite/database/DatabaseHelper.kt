package com.pritam.androidsqlitekotlin.sqlite.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pritam.androidsqlitekotlin.sqlite.database.model.Note
import java.util.ArrayList

/**
 * Created by ravi on 15/03/18.
 */

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object {

        // Database Version
        private val DATABASE_VERSION = 1

        // Database Name
        private val DATABASE_NAME = "notes_db"

        public val ID: String = "_id"
//        public val TIMESTAMP: String = "TIMESTAMP"
//        public val TEXT: String = "TEXT"


        private val TABLE_NAME: String = "notes"
        private val COLUMN_TIMESTAMP: String = "timestamp"
        private val COLUMN_ID: String = "id"
        private  val COLUMN_NOTE: String = "note"

        private val CREATE_TABLE = (
                    "CREATE TABLE " + TABLE_NAME + "("
                            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COLUMN_NOTE + " TEXT,"
                            + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                            + ")")
    }

    // Select All Query
    // looping through all rows and adding to list
    // close db connection
    // return notes list
    val allNotes: List<Note>
        get() {
            val notes = ArrayList<Note>()
            val selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " +
                    COLUMN_TIMESTAMP + " DESC"

            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val notei = Note(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)))

                    notes.add(notei)
                } while (cursor.moveToNext())
            }
            db.close()
            return notes
        }

    // return count
    val notesCount: Int
        get() {
            val countQuery = "SELECT  * FROM " + TABLE_NAME
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)

            val count = cursor.count
            cursor.close()
            return count
        }

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {

        // create notes table
        db.execSQL(CREATE_TABLE)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)

        // Create tables again
        onCreate(db)
    }


    fun insertNote(note: String): Long {
        // get writable database as we want to write data
        val db = this.writableDatabase

        val values = ContentValues()
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(COLUMN_NOTE, note)
        // insert row
        val id = db.insert(TABLE_NAME, null, values)

        // close db connection
        db.close()

        // return newly inserted row id
        return id
    }

    fun createNote(note: Note) : Long {
        // get writable database as we want to write data
        val db = this.writableDatabase

        val values = ContentValues()
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(COLUMN_ID, note.id)
        values.put(COLUMN_NOTE, note.note)
        values.put(COLUMN_TIMESTAMP, note.timestamp)
        // insert row
        val id = db.insert(TABLE_NAME, null, values)

        // close db connection
        db.close()

        // return newly inserted row id
        return id
    }

    fun getNote(id: Long): Note {
        // get readable database as we are not inserting anything
        val db = this.readableDatabase

        val cursor = db.query(TABLE_NAME,
                arrayOf(COLUMN_ID, COLUMN_NOTE, COLUMN_TIMESTAMP),
                COLUMN_ID + "=?",
                arrayOf(id.toString()), null, null, null, null)

        cursor?.moveToFirst()

        // prepare note object
        val note = Note(
                cursor!!.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)))

        // close the db connection
        cursor.close()

        return note
    }

    fun updateNote(note: Note): Int {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_NOTE, note.note)

        // updating row
        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                arrayOf(note.id.toString()))
    }

    fun deleteNote(note: Note) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                arrayOf(note.id.toString()))
        db.close()
    }

    fun deleteAllNote() {
        val db = this.writableDatabase
        //db.delete(TABLE_NAME, COLUMN_ID + " = ?", arrayOf(""))
        db.execSQL("delete from "+ TABLE_NAME + "; ");
        db.close()
    }


}
