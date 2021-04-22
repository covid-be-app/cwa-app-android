package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class ApplicationConfigurationInvalidException(message: String? = "the application configuration is invalid") :
    ReportedException(ErrorCodes.APPLICATION_CONFIGURATION_INVALID.code, message)


