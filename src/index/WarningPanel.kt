package index

import react.RBuilder
import react.dom.div
import react.dom.p

enum class WarningPanelLevel(val cssClass: String) {
    INFO("info"),
    WARNING("warning"),
    ERROR("error")
}

fun RBuilder.WarningPanel(content: String, warningPanelLevel: WarningPanelLevel = WarningPanelLevel.WARNING) {
    div("warning-panel ${warningPanelLevel.cssClass}") {
        p {
            +content
        }
    }
}