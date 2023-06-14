package com.coolwhitesd.iven

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        val addRowButton = findViewById<Button>(R.id.addRowButton)

        addRowButton.setOnClickListener {
            val tableRow = TableRow(this)
            val layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
            tableRow.layoutParams = layoutParams

            val editText1 = EditText(this)
            val editText2 = EditText(this)
            val deleteButton = Button(this).apply {
                text = "Delete"
                setOnClickListener { tableLayout.removeView(tableRow) }
            }

            editText2.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // No need to do anything here
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {
                        val color = Color.parseColor(s.toString())
                        editText1.setBackgroundColor(color)
                    } catch (e: IllegalArgumentException) {
                        val defaultColor = Color.parseColor("#FFFFFF")
                        editText1.setBackgroundColor(defaultColor)
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    // No need to do anything here
                }
            })


            tableRow.addView(editText1)
            tableRow.addView(editText2)
            tableRow.addView(deleteButton)

            tableLayout.addView(tableRow)
        }
    }
}
