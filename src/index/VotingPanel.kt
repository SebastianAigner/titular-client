package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

interface VotingPanelProps: RProps {
    var options: Map<String, String>
    var buttonPressHandler: (String) -> Unit
    var shouldShow: Boolean
    var shouldEnable: Boolean
}

class VotingPanel(props: VotingPanelProps): RComponent<VotingPanelProps, RState>(props) {
    override fun RBuilder.render() {
        props.options.map {
            child(Button::class) {
                attrs.label = if(props.shouldShow) it.value else "🤔"
                attrs.handleClick = {_ ->
                    props.buttonPressHandler(it.key)
                }
                attrs.disabled = (!props.shouldEnable) || !props.shouldShow
            }
        }
    }

}