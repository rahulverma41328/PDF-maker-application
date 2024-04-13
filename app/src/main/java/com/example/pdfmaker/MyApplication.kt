package com.example.pdfmaker

import android.app.Application
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

/*base class for maintaining global application state.
* we can provide our own implementation by creating a subclass and specifying the full-qualified name of this subclasses the "android:name"
* .attribute in your AndroidManifest
* we will define functions in this class that will be used in whole application, so we don't need to re write again and again*/
class MyApplication: Application() {

    public fun formatTimestamp(timestamp:Long):String{

        // get instance of calendar and set timestamp in it
        val calendar:Calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp
        // format timestamp to proper date format e.g. 29/10/2022
        val date:String = String.format("dd/MM/yyyy",calendar)

        return date
    }
}