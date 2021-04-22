package de.rki.coronawarnapp.service.applicationconfiguration

import be.sciensano.coronalert.risk.ExposureWindowRiskCalculationConfig
import be.sciensano.coronalert.risk.ExposureWindowRiskCalculationConfigMapper
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid

object ApplicationConfigurationService {
    suspend fun asyncRetrieveApplicationConfiguration(): AppConfigAndroid.ApplicationConfigurationAndroid {
        return WebRequestBuilder.getInstance().asyncGetApplicationConfigurationFromServer()
    }

    suspend fun asyncRetrieveExposureConfiguration(): ExposureWindowRiskCalculationConfig =
        ExposureWindowRiskCalculationConfigMapper().map(asyncRetrieveApplicationConfiguration())

}
