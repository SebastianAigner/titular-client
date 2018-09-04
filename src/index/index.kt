package index

import kotlinext.js.*
import org.w3c.dom.*
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

enum class SocketState {
    AWAITING,
    OPEN,
    CLOSED
}

interface AppState: RState {
    var socket: WebSocket
    var image: String
    var timeRemaining: Int
    var phase: GamePhase
    var guesses: Map<String, String>
    var players: Map<String, Player>
    var lobby: String?
    var points: Map<String, Pair<String, Int>>
    var canVote: Boolean
    var thisPlayerId: String
    var socketState: SocketState
}

class Player(var name: String, var points: Int, var lastRoundPoints: Int?)

interface AppProps: RProps {

}


enum class GamePhase(val desc: String) {
    NEED_NAME("Need name."),
    NEED_GAME_ID("Need Game Id."),
    WAITING_FOR_NEXT_ROUND("Waiting for next round"),
    GUESS("Enter your guess"),
    VOTE("Vote for the best results"),
    WAITING_FOR_FIRST_GAME("Waiting for first game")
}

val gameName = "Titular"

class App(props: AppProps): RComponent<AppProps, AppState>(props) {
    var timerId: Int? = null

    override fun AppState.init(props: AppProps) {
        val addr = js("process.env.REACT_APP_API_WEBSOCKET_ADDRESS")
        println("Working with API at $addr")
        val finalAddr = addr as? String ?: "ws://0.0.0.0:8080/myws/echo"
        socket = WebSocket(finalAddr)
        image = "https://via.placeholder.com/350x150"
        timeRemaining = 0
        phase = GamePhase.NEED_NAME
        guesses = mapOf()
        players = mapOf()
        points = mapOf()
        canVote = true
        socketState = SocketState.AWAITING
        socket.onopen = {
            setState {
                socketState = SocketState.OPEN
            }
        }
        socket.onclose = {
            if(it is CloseEvent) {
                println("close event ${it.reason}")
            }
            setState {
                socketState = SocketState.CLOSED
            }
        }
        socket.onmessage = {
            if(it is MessageEvent) {
                console.log(it.data)
                val str = it.data.toString().split(" ")
                when(str[0].toLowerCase()) {
                    "uuid" -> {
                        setState {
                            thisPlayerId = str[1]
                            phase = GamePhase.NEED_GAME_ID
                        }
                    }
                    "startround" -> {
                        setState {
                            guesses = mapOf()
                            phase = GamePhase.GUESS
                            players.values.forEach { it.lastRoundPoints = null }
                        }
                    }
                    "votenow" -> {
                        setState {
                            canVote = true
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

                    "player" -> {
                        setState {
                            players += str[1] to Player(str[2], str[3].toInt(), null)
                        }
                    }

                    "point" -> {
                        val uuid = str[1]
                        val amount = str[2]
                        setState {
                            players[uuid]?.let {
                                it.points += amount.toInt()
                                it.lastRoundPoints = amount.toInt()
                            }
                        }
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
        div("header") {
            h1 {
                +when(state.phase) {
                    GamePhase.NEED_NAME -> {
                        "$gameName – Welcome!"
                    }

                    GamePhase.NEED_GAME_ID -> {
                        "$gameName – Join a Lobby"
                    }
                    GamePhase.WAITING_FOR_NEXT_ROUND, GamePhase.WAITING_FOR_FIRST_GAME -> {
                        "$gameName: Lobby #${state.lobby}"
                    }
                    else -> {
                        gameName
                    }
                }
            }
        }

        when(state.socketState) {
            SocketState.AWAITING -> {
                WarningPanel("Connecting... Please wait.", WarningPanelLevel.INFO)
            }
            SocketState.CLOSED -> {
                WarningPanel("Connection to the server has been lost. Please reload the page.", WarningPanelLevel.ERROR)
            }
        }

        if(state.phase == GamePhase.NEED_NAME) {
            div("nameform") {
                h5 {
                    +"What is your name? 🕵🏻‍♂️"
                }
                child(SimpleInputField::class) {
                    attrs.handleNameAdd = {
                        val joined = it.replace(" ", "_")
                        println(joined)
                        state.socket.send("name $joined")
                    }
                    attrs.placeholder = listOf("Nathan Tailor", "Michael Scott", "SpicyBoi", "Existalgia", "Track Petchett", "Namey McNameface", "PinguLover447", "Blemished Hound", "Colorful Foxy", "Circularity", "Terry Crews", "RocketMind", "HowToMaster", "LudwigVanBeathoven", "Wayne_Gretzky", "Heavy_Weapons_Guy", "PinkyNBrain").shuffled().first()
                }
            }
        }

        /*child(VotingPanel::class) {
            attrs.shouldShow = true
            attrs.shouldEnable = true
            attrs.thisPlayerId = "none"
            attrs.options = mapOf("no" to "woooord jup", "jo" to "I'm freestyling, my little boi!", "ooo" to "This is the lonest answer my human mind can produce. This is the lonest answer my human mind can produce. This is the lonest answer my human mind can produce. This is the lonest answer my human mind can produce. This is the lonest answer my human mind can produce.")
        }*/

        if(state.phase == GamePhase.NEED_GAME_ID) {
            div("gameform") {
                h5 {
                    +"Please enter the Game ID. 🕹"
                }
                child(SimpleInputField::class) {
                    attrs.handleNameAdd = {
                        val joined = it.replace(" ", "_")
                        state.socket.send("game $joined")
                        setState {
                            phase = GamePhase.WAITING_FOR_FIRST_GAME
                            lobby = joined
                        }
                    }
                }
            }
        }

        if(state.phase == GamePhase.WAITING_FOR_NEXT_ROUND || state.phase == GamePhase.WAITING_FOR_FIRST_GAME) {
            child(Button::class) {
                attrs.label = "Start the round!"
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

        if(state.phase == GamePhase.VOTE) {
            h5 {
                +"vote for your favorite!"
            }
        }
        if(state.phase == GamePhase.GUESS) {
            h5 {
                +"taking suggestions..."
            }
        }
        if(state.phase == GamePhase.GUESS || state.phase == GamePhase.VOTE) {

            child(VotingPanel::class) {
                attrs.buttonPressHandler = {
                    state.socket.send("vote $it")
                    setState {
                        canVote = false
                    }
                }
                attrs.options = state.guesses
                attrs.shouldEnable = state.canVote
                attrs.shouldShow = state.phase == GamePhase.VOTE
                attrs.thisPlayerId = state.thisPlayerId
            }

        }

        if(state.players.size > 0) {
            child(Leaderboard::class) {
                attrs.players = state.players.map { x -> x.value.name to x.value.points }.toMap()
            }
        }

        if(state.phase == GamePhase.WAITING_FOR_NEXT_ROUND) {
            child(Evaluation::class) {
                attrs.players = state.players.filterValues { it.lastRoundPoints != null }.map {
                    it.value.name to Pair(state.guesses[it.key], it.value.lastRoundPoints)
                }.toMap()
            }
        }

        //sounds for each phase

        when(state.phase) {

            GamePhase.NEED_NAME -> {}
            GamePhase.NEED_GAME_ID -> {}
            GamePhase.WAITING_FOR_NEXT_ROUND -> {
                child(Sound::class) {
                    attrs.soundName = "notif.mp3"
                }
            }
            GamePhase.GUESS -> {
                child(Sound::class) {
                    attrs.soundName = "bel.mp3"
                }
            }
            GamePhase.VOTE -> {
                child(Sound::class) {
                    attrs.soundName = "hmm.mp3"
                }
            }
        }
        About()
    }
}




