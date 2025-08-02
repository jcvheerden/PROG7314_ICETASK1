package com.example.prjreading

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.concurrent.Executors
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpPost

class PoemsFragment : Fragment() {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var spnTitles: Spinner
    private lateinit var tvDisplay: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_poems, container, false)

        spnTitles = layout.findViewById<Spinner>(R.id.spnTitles)
        tvDisplay = layout.findViewById<Button>(R.id.tvDisplay)

        spnTitles.setOnClickListener {
            getAllTitles() //call the poems api to extract all the poems
        }
        return layout
    }

    //METHOD CONNECTING TO ALL TITLES ENDPOINT OF POETRY API
    private fun getAllTitles() {
        val url = "https://poetrydb.org/titles"

        executor.execute {
            url.httpGet().responseString { _, _, result ->
                handler.post {
                    when (result) {
                        is com.github.kittinunf.result.Result.Success -> {
                            val json = result.get()
                            try {
                                val poemTitles = Gson().fromJson(json, Array<Poetry>::class.java).toList()

                                if (poemTitles.isEmpty()) {
                                    tvDisplay.text = "No poems were extracted"
                                }
                                else {
                                    val poemTitleList = poemTitles.map { it.poemTitle }
                                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, poemTitleList)
                                    spnTitles.adapter = adapter
                                }
                            } catch (e: JsonSyntaxException) {
                                Log.e("GetPoems", "Json Parsing Error: ${e.message}")
                            }
                        }
                        is com.github.kittinunf.result.Result.Failure -> {
                            val ex = result.getException()
                            Log.e("GetPoems", "API Error: ${ex.message}")
                        }
                    }
                }
            }
        }
    }

    //METHOD TO PULL POEM BY THE TITLES
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