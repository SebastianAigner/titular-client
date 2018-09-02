package index

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.form
import react.dom.input

interface SimpleInputFieldProps: RProps {
    var handleNameAdd: (String) -> Any?
}

interface SimpleInputFieldState: RState {
    var textStuff: String
}

class SimpleInputField(props: SimpleInputFieldProps): RComponent<SimpleInputFieldProps, SimpleInputFieldState>(props) {

    override fun SimpleInputFieldState.init(props: SimpleInputFieldProps) {
        textStuff = ""
    }

    override fun RBuilder.render() {
        form {
            attrs {
                onSubmitFunction = {
                    it.preventDefault()
                    handleSubmit(it)
                }
            }
            input(type = InputType.text) {
                attrs {
                    name = "newElementText"
                    value = state.textStuff
                    onChangeFunction = ::handleChange
                }
            }
        }
    }

    fun handleChange(e: Event) {
        val html = e.target as HTMLInputElement
        val txt = html.value
        setState {
            textStuff = txt
        }
    }

    fun handleSubmit(e: Event) {
        setState {
            textStuff = ""
        }
        props.handleNameAdd(state.textStuff)
    }
}