package com.coolwhitesd.iven

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_OPEN_DOCUMENT = 1
        const val REQUEST_CODE_PERMISSIONS = 2
        const val APP_PREFS = "APP_PREFS"
        const val CSV_DATA = "CSV_DATA"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE)

        val calendarGrid = findViewById<GridLayout>(R.id.calendar_grid)
        val json = prefs.getString(CSV_DATA, "[]")
        val data: List<List<String>> = Gson().fromJson(json, object : TypeToken<List<List<String>>>() {}.type)
        buildCalendar(calendarGrid, LocalDate.now(), data)

        val btnBrowse: Button = findViewById(R.id.btn_browse)
        btnBrowse.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS)
            } else {
                openFilePicker()
            }
        }
        val btnSettings: Button = findViewById(R.id.btn_settings)
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values"))
        }
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val csvData = parseCsvFile(inputStream)
                    val json = Gson().toJson(csvData)
                    prefs.edit().putString(CSV_DATA, json).apply()

                    val calendarGrid = findViewById<GridLayout>(R.id.calendar_grid)
                    buildCalendar(calendarGrid, LocalDate.now(), csvData)
                }
            }
        }
    }

    private fun parseCsvFile(inputStream: InputStream): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.use {
            var line = it.readLine()
            while (line != null) {
                val tokens = line.split(',')
                result.add(tokens)
                line = it.readLine()
            }
        }
        return result
    }

    private fun buildCalendar(grid: GridLayout, date: LocalDate, data: List<List<String>> = emptyList()) {
        val inflater = LayoutInflater.from(this)
        grid.removeAllViews()

        val month = date.monthValue
        val year = date.year
        val firstOfMonth = LocalDate.of(year, month, 1)

        // Add day of week labels
        val dayOfWeekNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (name in dayOfWeekNames) {
            val dayOfWeekTextView = TextView(this)
            dayOfWeekTextView.text = name
            dayOfWeekTextView.gravity = Gravity.CENTER
            dayOfWeekTextView.layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setGravity(Gravity.FILL_HORIZONTAL)
            }
            grid.addView(dayOfWeekTextView)
        }

        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7

        for (i in 0 until dayOfWeek) {
            val blankDay = inflater.inflate(R.layout.calendar_item, grid, false)
            grid.addView(blankDay)
        }

        val daysInMonth = date.lengthOfMonth()
        for (i in 1..daysInMonth) {
            val dayLayout = inflater.inflate(R.layout.calendar_item, grid, false)

            val dayText: TextView = dayLayout.findViewById(R.id.dayTextView)
            dayText.text = i.toString()

            val textViewA: TextView = dayLayout.findViewById(R.id.textViewA)
            val textViewB: TextView = dayLayout.findViewById(R.id.textViewB)
            val textViewC: TextView = dayLayout.findViewById(R.id.textViewC)

            val matchingData = data.find { it[0] == LocalDate.of(year, month, i).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
            if (matchingData != null) {
                textViewA.text = matchingData[1]
                textViewB.text = matchingData[2]
                textViewC.text = matchingData[3]
            }

            grid.addView(dayLayout)
        }

        val rows = ceil((dayOfWeek + daysInMonth + 7) / 7.0).toInt() // Add 7 for the day of week row
        grid.rowCount = rows
    }
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }



}
