package index

import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.button
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

class SimpleInputField(props: SimpleInputFieldProps) : RComponent<SimpleInputFieldProps, SimpleInputFieldState>(props) {

    override fun SimpleInputFieldState.init(props: SimpleInputFieldProps) {
        currentText = ""
    }

    override fun RBuilder.render() {
        form {
            attrs {
                onSubmitFunction = {
                    it.preventDefault()
                    handleSubmit(it)
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
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +(props.callToAction ?: ">")
                        attrs {
                            disabled = state.currentText.isBlank()
                        }
                    }
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

    fun handleSubmit(e: Event) {
        setState {
            currentText = ""
        }
        props.handleNameAdd(state.currentText)
    }
}