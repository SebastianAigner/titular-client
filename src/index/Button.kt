package index

import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button

interface ButtonProps: RProps {
    var label: String
    var handleClick: (Event) -> Unit
    var disabled: Boolean
}

class Button(props: ButtonProps): RComponent<ButtonProps, RState>(props) {
    override fun RBuilder.render() {
        button {
            +props.label
            attrs {
                onClickFunction = props.handleClick
                disabled = props.disabled
            }
        }
    }

}