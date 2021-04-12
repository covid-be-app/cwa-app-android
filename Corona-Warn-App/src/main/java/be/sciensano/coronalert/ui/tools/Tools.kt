package be.sciensano.coronalert.ui.tools

import android.os.Parcelable
import de.rki.coronawarnapp.R
import kotlinx.android.parcel.Parcelize

@Parcelize
class ToolsChildren(val url: String, val titleRes: Int, val suffix: String? = null) : Parcelable

interface Tools : Parcelable {
    val illustrationRes: Int
    val titleRes: Int
    val children: List<ToolsChildren>
}

@Parcelize
data class VaccinationInformation(
    override val illustrationRes: Int = R.drawable.ic_tools_vaccination_info_illustration,
    override val titleRes: Int = R.string.tools_vaccination_information_title,
    override val children: List<ToolsChildren> = listOf(
        ToolsChildren(
            "https://datastudio.google.com/embed/u/0/reporting/c14a5cfc-cab7-4812-848c-0369173148ab/page/hOMwB",
            R.string.tools_epidemiological_situation
        ),
        ToolsChildren(
            "https://www.qvax.be/region",
            R.string.tools_register_to_be_vaccinated
        ),
    )
) : Tools


@Parcelize
data class TestReservation(
    override val illustrationRes: Int = R.drawable.ic_tools_test_reservation_illustration,
    override val titleRes: Int = R.string.tools_test_reservation_title,
    override val children: List<ToolsChildren> = listOf(
        ToolsChildren(
            "https://testcovid.doclr.be",
            R.string.tools_book_a_test,
        ),
        ToolsChildren(
            "https://brussels.testcovid.be/fr",
            R.string.tools_book_a_test_in_brussels,
        ),
    )
) : Tools


@Parcelize
data class QuarantineCertificate(
    override val illustrationRes: Int = R.drawable.ic_tools_quarantine_illustration,
    override val titleRes: Int = R.string.tools_quarantine_certification_title,
    override val children: List<ToolsChildren> = listOf(
        ToolsChildren(
            "https://www.zorg-en-gezondheid.be/testen-isoleren-en-quarantaine",
            R.string.tools_quarantine_certification_nl,
            "NL"
        ),
        ToolsChildren(
            "https://coronavirus.brussels/en/home-2/",
            R.string.tools_quarantine_certification_bxl,
            "BXL"
        ),
        ToolsChildren(
            "https://covid.aviq.be/fr/testing-particuliers",
            R.string.tools_quarantine_certification_wall,
            "WALL"
        ),
        ToolsChildren(
            "https://www.ostbelgienlive.be/desktopdefault.aspx/tabid-6711/",
            R.string.tools_quarantine_certification_de,
            "DE"
        ),
    )
) : Tools

@Parcelize
data class PassengerLocatorForm(
    override val illustrationRes: Int = R.drawable.ic_tools_passenger_locator_form_illustration,
    override val titleRes: Int = R.string.tools_passenger_locator_form_title,
    override val children: List<ToolsChildren> = listOf(
        ToolsChildren(
            "https://travel.info-coronavirus.be/nl/public-health-passenger-locator-form",
            R.string.tools_passenger_locator_form_nl,
            "NL"
        ),
        ToolsChildren(
            "https://travel.info-coronavirus.be/fr/public-health-passenger-locator-form",
            R.string.tools_passenger_locator_form_fr,
            "FR"
        ),

        ToolsChildren(
            "https://travel.info-coronavirus.be/de/public-health-passenger-locator-form",
            R.string.tools_passenger_locator_form_de,
            "DE"
        ),
        ToolsChildren(
            "https://travel.info-coronavirus.be/public-health-passenger-locator-form",
            R.string.tools_passenger_locator_form_en,
            "EN"
        ),
    )
) : Tools

@Parcelize
data class DeclarationOfHonour(
    override val illustrationRes: Int = R.drawable.ic_tools_declaration_of_honour_illustration,
    override val titleRes: Int = R.string.tools_declaration_of_honour_title,
    override val children: List<ToolsChildren> = listOf(
        ToolsChildren(
            "https://travel.info-coronavirus.be/nl/essentiele-reis",
            R.string.tools_declaration_of_honour_nl,
            "NL"
        ),
        ToolsChildren(
            "https://travel.info-coronavirus.be/fr/voyage-essentiel",
            R.string.tools_declaration_of_honour_fr,
            "FR"
        ),
        ToolsChildren(
            "https://travel.info-coronavirus.be/essential-travel-sworn-statement",
            R.string.tools_declaration_of_honour_en,
            "EN"
        ),
        ToolsChildren(
            "https://travel.info-coronavirus.be/de/notwendige-reise-ehrenwortliche-erklarung",
            R.string.tools_declaration_of_honour_de,
            "DE"
        ),
    )
) : Tools
