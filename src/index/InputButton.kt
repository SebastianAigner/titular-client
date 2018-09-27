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
    var inside: RBuilder.() -> Unit
    var handleClick: (Event) -> Unit
    var disabled: Boolean
    var classes: String?
    var type: ButtonType?
}

fun ButtonProps.myEq(other: ButtonProps): Boolean {
    return inside == other.inside && disabled == other.disabled && classes == other.classes && type == other.type
}

class InputButton(props: ButtonProps) : RComponent<ButtonProps, RState>(props) {
    override fun RBuilder.render() {
        button(classes = props.classes ?: "btn btn-primary", type = props.type) {
            props.inside(this)
            attrs {
                onClickFunction = props.handleClick
                disabled = props.disabled
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: ButtonProps, nextState: RState): Boolean {
        return !this.props.myEq(nextProps)
    }
}

fun RBuilder.inputButton(inside: RBuilder.() -> Unit, disabled: Boolean, classes: String? = null, type: ButtonType? = null, handleClick: (Event) -> Unit) = child(InputButton::class) {
    attrs.inside = inside
    attrs.disabled = disabled
    attrs.handleClick = handleClick
    attrs.classes = classes
    attrs.type = type
}