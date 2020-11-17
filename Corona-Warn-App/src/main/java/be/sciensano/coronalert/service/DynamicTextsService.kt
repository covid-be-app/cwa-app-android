package be.sciensano.coronalert.service

import android.content.Context
import be.sciensano.coronalert.http.responses.DynamicTexts
import com.google.gson.Gson
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

object DynamicTextsService {

    private const val CACHE_HOURS = 24

    val gson = Gson()

    private fun readDynamicTextsFile(file: File): DynamicTexts {
        val bufferedReader =
            BufferedReader(InputStreamReader(FileInputStream(file)))

        val dynamicTexts = gson.fromJson<DynamicTexts>(
            bufferedReader,
            DynamicTexts::class.java
        )
        bufferedReader.close()
        return dynamicTexts
    }

    private suspend fun fetchDynamicTextAndWrite(file: File): DynamicTexts {
        return withContext(Dispatchers.IO) {
            val response = WebRequestBuilder.getInstance().getDynamicTexts()

            val json = gson.toJson(response)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(json.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()

            response
        }
    }

    private fun readDefaultDynamicTexts(context: Context): DynamicTexts {
        val bufferedReader = context.assets.open("dynamicTexts.json").bufferedReader()
        val dynamicTexts = gson.fromJson(
            bufferedReader
                .use { it.readText() },
            DynamicTexts::class.java
        )

        bufferedReader.close()
        return dynamicTexts
    }

    suspend fun fetchDynamicTexts(context: Context): DynamicTexts {
        val file = File("${context.filesDir}/dynamicTexts.json")

        return try {

            if (file.exists()) {
                if (System.currentTimeMillis()
                        .millisecondsToHours() - file.lastModified()
                        .millisecondsToHours() < CACHE_HOURS
                ) {
                    readDynamicTextsFile(file)
                } else {
                    fetchDynamicTextAndWrite(file)
                }
            } else {
                fetchDynamicTextAndWrite(file)
            }
        } catch (e: Exception) {
            Timber.e(e)

            if (file.exists()) {
                readDynamicTextsFile(file)
            } else {
                readDefaultDynamicTexts(context)
            }
        }
    }

}
