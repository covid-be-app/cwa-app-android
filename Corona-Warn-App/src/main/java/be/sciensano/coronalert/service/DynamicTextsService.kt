package be.sciensano.coronalert.service

import android.content.Context
import be.sciensano.coronalert.http.responses.DynamicNews
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

        val dynamicTexts = gson.fromJson(
            bufferedReader,
            DynamicTexts::class.java
        )
        bufferedReader.close()
        return dynamicTexts
    }

    private fun readDynamicNewsFile(file: File): DynamicNews {
        val bufferedReader =
            BufferedReader(InputStreamReader(FileInputStream(file)))

        val dynamicTexts = gson.fromJson(
            bufferedReader,
            DynamicNews::class.java
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

    private suspend fun fetchDynamicNewsAndWrite(file: File): DynamicNews {
        return withContext(Dispatchers.IO) {
            val response = WebRequestBuilder.getInstance().getDynamicNews()

            val json = gson.toJson(response)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(json.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()

            response
        }
    }

    private fun readDefaultDynamicTexts(context: Context): DynamicTexts {
        val bufferedReader = context.assets.open("dynamicTextsV2.json").bufferedReader()
        val dynamicTexts = gson.fromJson(
            bufferedReader
                .use { it.readText() },
            DynamicTexts::class.java
        )

        bufferedReader.close()
        return dynamicTexts
    }

    private fun readDefaultDynamicNews(context: Context): DynamicNews {
        val bufferedReader = context.assets.open("dynamicNews.json").bufferedReader()
        val dynamicNews = gson.fromJson(
            bufferedReader
                .use { it.readText() },
            DynamicNews::class.java
        )

        bufferedReader.close()
        return dynamicNews
    }

    suspend fun fetchDynamicNews(context: Context): DynamicNews? {
        val file = File("${context.filesDir}/dynamicNews.json")

        return try {
            if (file.exists()) {
                if (System.currentTimeMillis()
                        .millisecondsToHours() - file.lastModified()
                        .millisecondsToHours() < CACHE_HOURS
                ) {
                    readDynamicNewsFile(file)
                } else {
                    fetchDynamicNewsAndWrite(file)
                }
            } else {
                fetchDynamicNewsAndWrite(file)
            }
        } catch (e: Exception) {
            Timber.e(e)

            if (file.exists()) {
                readDynamicNewsFile(file)
            } else {
                readDefaultDynamicNews(context)
            }
        }
    }

    suspend fun fetchDynamicTexts(context: Context): DynamicTexts {
        val file = File("${context.filesDir}/dynamicTextsV2.json")

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
