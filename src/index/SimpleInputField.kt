package index

import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import react.dom.form
import react.dom.input

interface SimpleInputFieldProps : RProps {
    var handleNameAdd: (String) -> Any?
    var placeholder: String?
    var callToAction: String?
}

interface SimpleInputFieldState : RState {
    var currentText: String
}

fun RBuilder.simpleInputField(callToAction: String? = null, placeholder: String? = null, handleSubmit: (String) -> Any?) = child(SimpleInputField::class) {
    attrs.callToAction = callToAction
    attrs.placeholder = placeholder
    attrs.handleNameAdd = handleSubmit
}

class SimpleInputField(props: SimpleInputFieldProps) : RComponent<SimpleInputFieldProps, SimpleInputFieldState>(props) {

    override fun SimpleInputFieldState.init(props: SimpleInputFieldProps) {
        currentText = ""
    }

    override fun RBuilder.render() {
        form {
            attrs {
                onSubmitFunction = {
                    it.preventDefault()
                    handleSubmit()
                }
            }
            div("input-group") {
                input(type = InputType.text, classes = "form-control") {
                    attrs {
                        name = "newElementText"
                        value = state.currentText
                        onChangeFunction = ::handleChange
                        autoComplete = false
                        props.placeholder?.let {
                            placeholder = it
                        }
                    }
                }
                div("input-group-append") {
                    inputButton({
                        +(props.callToAction ?: ">")
                    }, state.currentText.isBlank(), "btn btn-primary", ButtonType.submit) {}
                }
            }
        }
    }

    fun handleChange(e: Event) {
        val html = e.target as HTMLInputElement
        val txt = html.value
        setState {
            currentText = txt
        }
    }

    fun handleSubmit() {
        setState {
            currentText = ""
        }
        props.handleNameAdd(state.currentText)
    }
}