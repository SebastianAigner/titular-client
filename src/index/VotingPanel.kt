package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div

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
                    child(Button::class) {
                        attrs.classes = "btn btn-primary btn-lg btn-block multiline mb-1"
                        attrs.label = if (props.shouldShow) it.value else "🤔"
                        attrs.handleClick = { _ ->
                            props.buttonPressHandler(it.key)
                        }
                        attrs.disabled = (!props.shouldEnable) || !props.shouldShow || props.thisPlayerId == it.key
                    }
                    if (!props.shouldShow) {
                        child(Sound::class) {
                            attrs.soundName = "write.mp3"
                        }
                    }
                }
            }
        }
    }
}