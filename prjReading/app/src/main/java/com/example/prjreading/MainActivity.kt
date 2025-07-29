package com.example.prjreading

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //tvTitle = findViewById<TextView>(R.id.tvTitle>)
    }

    //METHOD TO PULL POEMS BY THE TITLES
    private fun getPoemByTitle(title: String) {
        //giving the url/endpoint that has to be accessed within the api
        val url = "https://poetrydb.org/title/${title}" //endpoint to return a poem in the poetry db
        //INSERT A TEXT DISPLAY FOR THE POEMS DETAILS

        executor.execute { //extra thread to separate ui from background processing
            //use the new gradle imports (fuel) to access request types
            url.httpGet().responseString { _, _, result ->
                //update GUI on a different thread
                handler.post {
                    when (result) {
                        is com.github.kittinunf.result.Result.Success -> {
                            //on success, deserialize the json string into a poem object
                            val json = result.get()
                            try {
                                val poem = Gson().fromJson(
                                    json,
                                    Poetry::class.java
                                ) //pull the individual details of the poem into an object
                                //format the display for readability
                                val neatDisplay =
                                    "${poem.poemTitle}\nBy ${poem.authorName}\n\n${poem.lines}\nNr of Lines: ${poem.lineCount}" //pull out individual strips of info into an object
                                //UPDATE THE TEXT AREA TO DISPLAY THE POEM

                            } catch (e: JsonSyntaxException) { //if the json string wasnt formatted correctly, catch the error and print back the message
                                Log.e("GetPoems", "Json Parsing Error: ${e.message}")
                            }
                        }

                        is com.github.kittinunf.result.Result.Failure -> { //response from server when calling the api (response codes 200,400,500 etc)
                            //on failure log the error and show user friendly message
                            val ex = result.getException()
                            Log.e("GetPoems", "API Error: ${ex.message}")
                            //UPDATE TEXT AREA TO EXPLAIN AN ERROR OCCURRED
                        }
                    }
                }
            }
        }
    }
}