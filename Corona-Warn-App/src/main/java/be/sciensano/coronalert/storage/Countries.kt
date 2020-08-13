package be.sciensano.coronalert.storage

import android.content.Context
import be.sciensano.coronalert.ui.submission.Country
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.R
import java.io.BufferedReader
import java.io.InputStreamReader

fun readCountries(context: Context): List<Country> {
    val raw = context.resources.openRawResource(R.raw.countries)
    val reader = BufferedReader(InputStreamReader(raw))
    val gson = Gson()
    val itemType = object : TypeToken<List<Country>>() {}.type
    return gson.fromJson<List<Country>>(reader, itemType)
}