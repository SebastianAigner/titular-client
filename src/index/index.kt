package index

import kotlinext.js.*
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import kotlinx.html.style
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import kotlin.browser.*

fun main(args: Array<String>) {
    requireAll(require.context("src", true, js("/\\.css$/")))

    render(document.getElementById("root")) {
        child(App::class) {

        }
    }
}

interface ImageBoxProps: RProps {
    var image: String
}

class ImageBox(props: ImageBoxProps): RComponent<ImageBoxProps, RState>(props) {
    override fun RBuilder.render() {
        div("imagebox") {
            img(src=props.image) {

            }
        }
    }
}

interface AppState: RState {
    var socket: WebSocket
    var image: String
    var timeRemaining: Int
    var phase: GamePhase
    var guesses: Map<String, String>
}

interface AppProps: RProps {

}

enum class GamePhase(val desc: String) {
    NEED_NAME("Need name."),
    NEED_GAME_ID("Need Game Id."),
    WAITING_FOR_NEXT_ROUND("Waiting for next round"),
    GUESS("Enter your guess"),
    VOTE("Vote for the best results")
}

class App(props: AppProps): RComponent<AppProps, AppState>(props) {
    var timerId: Int? = null

    override fun AppState.init(props: AppProps) {
        socket = WebSocket("ws://0.0.0.0:8080/myws/echo")
        image = "https://via.placeholder.com/350x150"
        timeRemaining = 0
        phase = GamePhase.NEED_NAME
        guesses = mapOf()

        socket.onmessage = {
            if(it is MessageEvent) {
                console.log(it.data)
                val str = it.data.toString().split(" ")
                when(str[0].toLowerCase()) {
                    "startround" -> {
                        setState {
                            phase = GamePhase.GUESS
                        }
                    }
                    "votenow" -> {
                        setState {
                            phase = GamePhase.VOTE
                        }

                    }
                    "voteend" -> {
                        setState {
                            phase = GamePhase.WAITING_FOR_NEXT_ROUND
                        }
                    }

                    "guess" -> {
                        setState {
                            guesses += str[1] to it.data.toString().substringAfter(" ").substringAfter(" ")
                        }
                        println(state.guesses)
                    }

                    "image" -> {
                        setState {
                            image = str[1]
                        }
                    }
                    "time" -> {
                        timerId?.let {
                            window.clearInterval(it)
                        }
                        setState {
                            timeRemaining = str[1].toInt()
                        }
                        timerId = window.setInterval({

                            setState {
                                if(state.timeRemaining > 0) {
                                    timeRemaining -= 1
                                }
                                else {
                                    timerId?.let {
                                        window.clearInterval(it)
                                    }
                                }
                            }
                        }, 1000)
                    }
                }
            }
        }
    }
    override fun RBuilder.render() {
        h1 {
            +"Game"
        }

        div("statusbox") {
            p {
                +state.phase.desc
            }
        }

        if(state.phase == GamePhase.NEED_NAME) {
            div("nameform") {
                h5 {
                    +"enter name"
                }
                child(SimpleInputField::class) {
                    attrs.handleNameAdd = {
                        state.socket.send("name $it")
                        setState {
                            phase = GamePhase.NEED_GAME_ID
                        }
                    }
                }
            }
        }

        if(state.phase == GamePhase.NEED_GAME_ID) {
            div("gameform") {
                h5 {
                    +"enter game id"
                }
                child(SimpleInputField::class) {
                    attrs.handleNameAdd = {
                        state.socket.send("game $it")
                        setState {
                            phase = GamePhase.WAITING_FOR_NEXT_ROUND
                        }
                    }
                }
            }
        }

        if(state.phase == GamePhase.WAITING_FOR_NEXT_ROUND) {
            child(Button::class) {
                attrs.label = "Start Game"
                attrs.handleClick = {
                    state.socket.send("start")
                }
            }
        }

        if(state.phase == GamePhase.GUESS || state.phase == GamePhase.VOTE) {
            h5 {
                +"observe image"
            }
            child(ImageBox::class) {
                attrs.image = state.image
            }
            h3 {
                +"${state.timeRemaining}s remaining"
            }
        }

        if(state.phase == GamePhase.GUESS) {
            h5 {
                +"enter guess"
            }
            child(SimpleInputField::class) {
                attrs.handleNameAdd = {
                    state.socket.send("guess $it")
                }
            }
        }

        if(/*state.phase == GamePhase.VOTE*/ true) {
            h5 {
                +"vote for your favorite!"
            }
            child(VotingPanel::class) {
                attrs.buttonPressHandler = {
                    state.socket.send("vote $it")
                }
                attrs.options = state.guesses
            }
        }
    }
}

interface ButtonProps: RProps {
    var label: String
    var handleClick: (Event) -> Unit
}

class Button(props: ButtonProps): RComponent<ButtonProps, RState>(props) {
    override fun RBuilder.render() {
        button {
            +props.label
            attrs {
                onClickFunction = props.handleClick
            }
        }
    }

}

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

interface VotingPanelProps: RProps {
    var options: Map<String, String>
    var buttonPressHandler: (String) -> Unit
}

class VotingPanel(props: VotingPanelProps): RComponent<VotingPanelProps, RState>(props) {
    override fun RBuilder.render() {
        props.options.map {
            child(Button::class) {
                attrs.label = it.value
                attrs.handleClick = {_ ->
                    props.buttonPressHandler(it.key)
                }
            }
        }
    }

}