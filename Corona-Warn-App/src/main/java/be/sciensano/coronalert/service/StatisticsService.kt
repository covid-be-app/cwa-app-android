package be.sciensano.coronalert.service

import android.content.Context
import be.sciensano.coronalert.util.DateUtil
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

object StatisticsService {

    private const val CACHE_HOURS = 1

    val gson = Gson()

    private fun readStatisticsFile(file: File): Statistics {
        val bufferedReader =
            BufferedReader(InputStreamReader(FileInputStream(file)))

        val statistics = gson.fromJson(
            bufferedReader,
            Statistics::class.java
        )
        bufferedReader.close()
        return statistics
    }

    suspend fun fetchStatisticsAndWrite(file: File): Statistics {
        return withContext(Dispatchers.IO) {
            val response = WebRequestBuilder.getInstance().getStatistics()

            val statistics =
                Statistics(
                    response.averageInfected,
                    response.averageInfectedChangePercentage,
                    response.averageHospitalised,
                    response.averageHospitalisedChangePercentage,
                    response.averageDeceased,
                    response.averageDeceasedChangePercentage,
                    response.atLeastPartiallyVaccinated,
                    response.fullyVaccinated,
                    response.boosterVaccinated,
                    DateUtil.parseServerDate(response.startDate).toDate().time,
                    DateUtil.parseServerDate(response.endDate).toDate().time,
                    System.currentTimeMillis()
                )

            val json = gson.toJson(statistics)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(json.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()

            statistics
        }
    }

    suspend fun fetchStatistics(context: Context): Statistics? {
        val file = File("${context.filesDir}/statistics.json")

        return try {
            if (file.exists()) {
                if (System.currentTimeMillis()
                        .millisecondsToHours() - file.lastModified()
                        .millisecondsToHours() < CACHE_HOURS
                ) {
                    readStatisticsFile(file)
                } else {
                    fetchStatisticsAndWrite(file)
                }
            } else {
                fetchStatisticsAndWrite(file)
            }
        } catch (e: Exception) {
            Timber.e(e)

            if (file.exists()) {
                readStatisticsFile(file)
            } else {
                null
            }
        }
    }
}
