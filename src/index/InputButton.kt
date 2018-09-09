package index

import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button

interface ButtonProps : RProps {
    var label: String
    var handleClick: (Event) -> Unit
    var disabled: Boolean
    var classes: String?
    var type: ButtonType?
}

class InputButton(props: ButtonProps) : RComponent<ButtonProps, RState>(props) {
    override fun RBuilder.render() {
        button(classes = props.classes ?: "btn btn-primary", type = props.type) {
            +props.label
            attrs {
                onClickFunction = props.handleClick
                disabled = props.disabled
            }
        }
    }
}

fun RBuilder.inputButton(label: String, disabled: Boolean, classes: String? = null, type: ButtonType? = null, handleClick: (Event) -> Unit) = child(InputButton::class) {
    attrs.label = label
    attrs.disabled = disabled
    attrs.handleClick = handleClick
    attrs.classes = classes
    attrs.type = type
}