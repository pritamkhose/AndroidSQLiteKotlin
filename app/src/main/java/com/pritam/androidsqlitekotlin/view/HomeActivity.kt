package com.pritam.androidsqlitekotlin.view

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.pritam.androidsqlitekotlin.R
import com.pritam.androidsqlitekotlin.sqlite.database.DatabaseHelper
import com.pritam.androidsqlitekotlin.sqlite.database.model.Note
import com.pritam.androidsqlitekotlin.sqlite.utils.MyDividerItemDecoration
import com.pritam.androidsqlitekotlin.sqlite.utils.RecyclerTouchListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.note_dialog.view.*
import java.util.*

import com.google.gson.JsonParser
import com.google.gson.JsonArray
import android.R.string.cancel
import android.util.Log

import android.app.ProgressDialog
import com.google.gson.Gson
import com.google.gson.JsonObject

import java.util.concurrent.TimeUnit

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter


class HomeActivity : AppCompatActivity() {

    private var mAdapter: NotesAdapter? = null
    private val notesList = ArrayList<Note>()
    private var db: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            showNoteDialog(false, null, -1)
        }


        db = DatabaseHelper(this)

        notesList.addAll(db!!.allNotes)

        mAdapter = NotesAdapter(this@HomeActivity, notesList)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recycler_view.setLayoutManager(mLayoutManager)
        recycler_view.setItemAnimator(DefaultItemAnimator())
        recycler_view.addItemDecoration(MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16))
        recycler_view.setAdapter(mAdapter)

        toggleEmptyNotes()

        /*
         * On long press on RecyclerView item, open alert dialog  with options to choose Edit and Delete
         */

        recycler_view.addOnItemTouchListener(RecyclerTouchListener(this@HomeActivity, recycler_view, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                showActionsDialog(position)
            }

            override fun onLongClick(view: View?, position: Int) {
                showActionsDialog(position)
            }
        }))

        progress = ProgressDialog(this);
        //progress!!.setTitle("Loading");
        progress!!.setMessage("Please wait while loading...");
        progress!!.setCancelable(false);


    }

    private fun showNoteDialog(shouldUpdate: Boolean, noteObj: Note?, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(applicationContext)
        val view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null)

        val alertDialogBuilderUserInput = AlertDialog.Builder(this@HomeActivity)
        alertDialogBuilderUserInput.setView(view)

        view.dialog_title.setText(if (!shouldUpdate) getString(R.string.lbl_new_note_title) else getString(R.string.lbl_edit_note_title))

        if (shouldUpdate && noteObj != null) {
            view.note.setText(noteObj.note);
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(if (shouldUpdate) "update" else "save")
                { dialogBox, id -> }
                .setNegativeButton("cancel")
                { dialogBox, id -> dialogBox.cancel() }

        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            // Show toast message when no text is entered
            if (TextUtils.isEmpty(view.note.text.toString())) {
                Toast.makeText(this@HomeActivity, "Enter note!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else {
                alertDialog.dismiss()
            }

            // check if user updating note
            if (shouldUpdate && view.note.text.toString() != null) {
                // update note by it's id
                updateNote(view.note.text.toString(), position)
            } else {
                // create new note
                createNote(view.note.text.toString())
            }
        })
    }

    private fun updateNote(text: String, position: Int) {
        val n = notesList.get(position)
        // updating note text
        n.note = text

        // updating note in db
        db!!.updateNote(n)

        // refreshing the list
        notesList.set(position, n)
        mAdapter?.notifyItemChanged(position)

        toggleEmptyNotes()
    }

    private fun createNote(text: String) {
        // inserting note in db and getting
        // newly inserted note id
        val id = db!!.insertNote(text)

        // get the newly inserted note from db
        val n = db!!.getNote(id)

        if (n != null) {
            // adding new note to array list at 0 position
            notesList.add(0, n)

            // refreshing the list
            mAdapter?.notifyDataSetChanged()

            toggleEmptyNotes()
        }
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private fun showActionsDialog(position: Int) {
        val colors = arrayOf<CharSequence>("Edit", "Delete")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose option")
        builder.setItems(colors) { dialog, which ->
            if (which == 0) {
                showNoteDialog(true, notesList[position], position)
            } else {
                deleteNote(position)
            }
        }
        builder.show()
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private fun deleteNote(position: Int) {
        // deleting the note from db
        db!!.deleteNote(notesList[position])

        // removing the note from the list
        notesList.removeAt(position)
        mAdapter?.notifyItemRemoved(position)

        toggleEmptyNotes()
    }


    /**
     * Toggling list and empty notes view
     */
    private fun toggleEmptyNotes() {
        // you can check notesList.size() > 0

        if (db!!.notesCount > 0) {
            empty_notes_view.setVisibility(View.GONE)
        } else {
            empty_notes_view.setVisibility(View.VISIBLE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                    System.exit(0);
                } else {
                    doexit()
                }
                true
            }
            R.id.action_backup -> {
                if (isNetworkAvaiable()) {
                    http_post_request()
                } else {
                    alertDialog();
                }
                true
            }
            R.id.action_restore -> {
                if (isNetworkAvaiable()) {
                    http_get_request()
                } else {
                    alertDialog();
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    var progress: ProgressDialog? = null
    var myResponse: String = ""
    private fun http_get_request() {
        try {
            progress!!.show()
            myResponse = ""
            val url = "https://angular-db-fa163.firebaseio.com/androidsqlitekotlin/user1.json"

            val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

            val request = Request.Builder()
                    .url(url)
                    .build()
            Log.d("-->>", request.toString())

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()
                    progress!!.dismiss()
                    Log.d("-->>", getStackTrace(e))
                    alertDialog("Request Failure", getStackTrace(e))
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    progress!!.dismiss()

                    this@HomeActivity.runOnUiThread(Runnable {

                        try {
                            //JsonObject newObj = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                            //val newArr = JsonParser().parse(response.body().toString()).getAsJsonArray()
                            val newObj = JsonParser().parse(response.body()!!.string()).getAsJsonObject()
                            myResponse = newObj.toString()
                            Log.d("-->>", myResponse)
                            updateNetNote(newObj)
                        } catch (e: Exception) {
                            Log.d("-->>", getStackTrace(e))
                            alertDialog("Server Data Not Available", getStackTrace(e))
                        }
                    }
                    )

                }
            })
        } catch (e: Exception) {
            progress!!.dismiss()
            Log.d("-->>", getStackTrace(e))
            alertDialog("Exception", getStackTrace(e))
        }

    }

    private fun updateNetNote(newObj: JsonObject?) {
        db!!.deleteAllNote()

        if (newObj != null) {
            val jArr = newObj.get("user1").asJsonArray
            for (i in 0 until jArr.size()) {
                val jObj = jArr[i].asJsonObject
                val note = Note(jObj.get("id").asInt, jObj.get("note").asString, jObj.get("timestamp").asString)
                // inserting note in db
                val id = db!!.createNote(note)
            }
        }

        // refreshing the list
        notesList.clear()
        notesList.addAll(db!!.allNotes)

//        mAdapter = NotesAdapter(this@HomeActivity, notesList)
//        recycler_view.setAdapter(mAdapter)

        mAdapter?.notifyDataSetChanged()
        toggleEmptyNotes()
    }

    private fun http_post_request() {
        try {
            progress!!.show()
            myResponse = ""
            val url = "https://angular-db-fa163.firebaseio.com/androidsqlitekotlin/user1.json"

            val postBody: String = getBody()

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postBody)

            val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

            val request = Request.Builder()
                    .url(url)
                    .put(body)
                    .build()
            Log.d("-->>", request.toString())

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()
                    progress!!.dismiss()
                    Log.d("-->>", getStackTrace(e))
                    alertDialog("Request Failure", getStackTrace(e))
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    progress!!.dismiss()
                    myResponse = response.toString() + "\n"

                    try {
                        //JsonObject newObj = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                        val newArr = JsonParser().parse(response.body().toString()).getAsJsonArray()
                        myResponse += newArr.toString()
                    } catch (e: Exception) {
                        Log.d("-->>", getStackTrace(e))
                        myResponse += response.body().toString()
                    }

                    this@HomeActivity.runOnUiThread(Runnable {
                        //text_response.setText(myResponse)
                        Log.d("-->>", myResponse)
                    }
                    )

                }
            })
        } catch (e: Exception) {
            progress!!.dismiss()
            Log.d("-->>", getStackTrace(e))
            alertDialog("Exception", getStackTrace(e))
        }
    }

    private fun getBody(): String {
        val hm: HashMap<String, Any> = HashMap<String, Any>()
        hm.put("user1", notesList)
        return Gson().toJson(hm).toString()
    }

    private fun getStackTrace(aThrowable: Throwable): String {
        val result = StringWriter()
        val printWriter = PrintWriter(result)
        aThrowable.printStackTrace(printWriter)
        return result.toString()
    }


    private fun alertDialog() {
        Snackbar.make(getWindow().getDecorView().getRootView(), "No Internet Connection", Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    private fun alertDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this@HomeActivity)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("cancel")
                { dialogBox, id -> dialogBox.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun isNetworkAvaiable(): Boolean {
        val cm = getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        return if (activeNetwork != null && activeNetwork.isConnected) {
            true
        } else false

    }

    private fun doexit() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
