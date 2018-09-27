package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.p

interface VotingPanelProps : RProps {
    var options: Map<String, String>
    var buttonPressHandler: (String) -> Unit
    var shouldShow: Boolean
    var shouldEnable: Boolean
    var thisPlayerId: String
}

class VotingPanel(props: VotingPanelProps) : RComponent<VotingPanelProps, RState>(props) {
    override fun RBuilder.render() {

        div("voting-panel") {
            div {
                props.options.map {
                    val disabled = (!props.shouldEnable) || !props.shouldShow || props.thisPlayerId == it.key
                    var label: RBuilder.() -> Unit =
                            if (props.shouldShow) {
                                { +it.value }
                            } else {
                                {
                                    emoji("thinking-face")
                                }
                            }
                    inputButton(label, disabled, "btn btn-primary btn-lg btn-block multiline mb-1") { e ->
                        props.buttonPressHandler(it.key)
                    }
                    if (!props.shouldShow) {
                        sound("write.mp3")
                    }
                }
            }
        }
    }
}

fun RBuilder.wank() {
    p {

    }
}

fun RBuilder.thinkingEmoji() {

}

fun RBuilder.moj(str: String) {
    p {

    }
}