package index

import react.RBuilder
import react.dom.div
import react.dom.h4
import react.dom.p

enum class WarningPanelLevel(val cssClass: String) {
    INFO("alert-light"),
    WARNING("alert-warning"),
    ERROR("alert-danger")
}

fun RBuilder.WarningPanel(title: String, information: String? = null, warningPanelLevel: WarningPanelLevel = WarningPanelLevel.WARNING) {
    div("warning-panel alert ${warningPanelLevel.cssClass}") {
        h4("alert-heading") {
            +title
        }
        information?.let {
            p {
                +information
            }
        }
    }
}