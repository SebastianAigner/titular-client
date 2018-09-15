package index

import kotlinext.js.require
import kotlinext.js.requireAll
import org.w3c.dom.CloseEvent
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.Window
import react.*
import react.dom.*
import kotlin.browser.document
import kotlin.browser.window

@JsModule("react")
external val thisReact: Any

@JsModule("why-did-you-update")
external val why_did_you_update: Any
val enable_why_did_you_update = why_did_you_update.asDynamic().default

fun main(args: Array<String>) {
    requireAll(require.context("src", true, js("/\\.css$/")))
    if (js("process.env.REACT_APP_API_WEBSOCKET_ADDRESS") as? String ?: "dev" != "production") {
        enable_why_did_you_update(thisReact)
    }

    render(document.getElementById("root")) {
        child(App::class) {}
    }
}

enum class SocketState {
    AWAITING,
    OPEN,
    CLOSED
}

enum class GameMode(val humanDesc: String, val details: String) {
    TOP_ALL_TIME("All Time", "Get a selection of the all-time best WikiHow illustrations, curated by the Reddit Community on /r/disneyvacation."),
    TOP_THIS_MONTH("Month", "Played through the all-time best? The monthly top content provides great illustrations that are fresh on a more frequent basis."),
    TOP_THIS_WEEK("Week", "The weekly top - not as curated as monthly or all time, but fresh!"),
    HOT("Hot (today)", "Cutting-edge illustrations straight from the tap!"),
    HMMM("Bonus: Hmmm", "Images that really make you go 🤔. Courtesy of /r/hmmm."),
    NOTDISNEY_ALL_TIME("Bonus: Not Disney (All Time)", "/r/notdisneyvacation - since we're making up the titles ourselves.")
}

interface AppState : RState {
    var image: String
    var timeRemaining: Int
    var phase: GamePhase
    var guesses: Map<String, String>
    var players: Map<String, Player>
    var lobby: String?
    var points: Map<String, Pair<String, Int>>
    var canVote: Boolean
    var didVote: Boolean
    var thisPlayerId: String
    var socketState: SocketState
    var didPlayerJustLeave: Boolean
    var hasSubmittedGuess: Boolean
    var votes: Int
    var currentGameMode: GameMode
    var interactAllowed: Boolean
    var availableLobbies: List<Pair<String, Int>>
    var totalNumberOfPlayers: Int
    var namePlaceholder: String
}

class Player(var name: String, var points: Int, var lastRoundPoints: Int?)

const val gameName = "Titular"

class App(props: RProps) : RComponent<RProps, AppState>(props) {
    var timerId: Int? = null
    val socket = WebSocket(js("process.env.REACT_APP_API_WEBSOCKET_ADDRESS") as? String ?: "ws://0.0.0.0:8080/")

    init {
        socket.onopen = {
            setState {
                socketState = SocketState.OPEN
            }
        }

        socket.onclose = {
            if (it is CloseEvent) {
                println("Socket Close Event: ${it.reason}")
            }
            setState {
                socketState = SocketState.CLOSED
            }
        }

        socket.onmessage = {
            if (it is MessageEvent) {
                println(it.data)
                handleMessage(it)
            } else {
                println(it.toString())
            }
        }
    }

    override fun AppState.init(props: RProps) {
        image = "https://via.placeholder.com/350x150"
        timeRemaining = 0
        phase = GamePhase.NEED_NAME
        guesses = mapOf()
        players = mapOf()
        totalNumberOfPlayers = 0
        points = mapOf()
        canVote = true
        didVote = false
        interactAllowed = true
        hasSubmittedGuess = false
        votes = 0
        currentGameMode = GameMode.TOP_ALL_TIME
        socketState = SocketState.AWAITING
        availableLobbies = listOf()
        namePlaceholder = listOf("Nathan Tailor", "Michael Scott", "SpicyBoi", "Existalgia", "Track Petchett", "Namey McNameface", "PinguLover447", "Blemished Hound", "Colorful Foxy", "Circularity", "Terry Crews", "RocketMind", "HowToMaster", "LudwigVanBeathoven", "Wayne_Gretzky", "Heavy_Weapons_Guy", "PinkyNBrain").shuffled().first()
    }

    fun handleMessage(it: MessageEvent) {
        val str = it.data.toString().split(" ")
        when (str[0].toLowerCase()) {
            "uuid" -> setState {
                thisPlayerId = str[1]
                phase = GamePhase.NEED_GAME_ID
            }
            "gamemode" -> setState {
                currentGameMode = GameMode.valueOf(str[1])
            }

            "nointeract" -> setState {
                interactAllowed = false
            }

            "interact" -> setState {
                interactAllowed = true
            }

            "lobby" -> setState {
                availableLobbies += Pair(str[1], str[2].toInt())
            }

            "noplayers" -> {
                val new = str[1].toInt()
                if(state.totalNumberOfPlayers != new) {
                    setState {
                        totalNumberOfPlayers = str[1].toInt()
                    }
                }
            }

            "joined" -> setState {
                lobby = str[1]
                phase = GamePhase.WAITING_FOR_FIRST_GAME
            }

            "startround" -> setState {
                guesses = mapOf()
                phase = GamePhase.GUESS
                hasSubmittedGuess = false
                players.values.forEach { it.lastRoundPoints = null }
                votes = 0
                didVote = false
            }

            "votenow" -> setState {
                canVote = true
                phase = GamePhase.VOTE
            }


            "voteend" -> setState {
                phase = GamePhase.WAITING_FOR_NEXT_ROUND
                timeRemaining = 0
            }

            "vote_indicator" -> setState {
                votes++
            }

            "guess" -> setState {
                guesses += str[1] to it.data.toString().substringAfter(" ").substringAfter(" ")
            }

            "player" -> setState {
                players += str[1] to Player(str[2], str[3].toInt(), null)
            }

            "player_leave" -> setState {
                players = players.filterNot { pl -> pl.key == str[1] }
                didPlayerJustLeave = true
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
            "image" -> setState {
                image = str[1]
            }

            "time" -> {
                timerId?.let {
                    window.clearInterval(it)
                }
                setState {
                    timeRemaining = str[1].toInt()
                }

                timerId = window.setMyInterval(1000) {
                    setState {
                        if (state.timeRemaining > 0) {
                            timeRemaining -= 1
                        } else {
                            timerId?.let {
                                window.clearInterval(it)
                            }
                        }
                    }
                }
            }
        }
    }


    fun RBuilder.soundEffects() {
        when (state.phase) {
            GamePhase.NEED_NAME, GamePhase.NEED_GAME_ID, GamePhase.WAITING_FOR_FIRST_GAME -> {
            }
            GamePhase.WAITING_FOR_NEXT_ROUND -> child(Sound::class) {
                attrs.soundName = "notif.mp3"
            }
            GamePhase.GUESS -> child(Sound::class) {
                attrs.soundName = "bel.mp3"
            }

            GamePhase.VOTE -> child(Sound::class) {
                attrs.soundName = "hmm.mp3"
            }
        }

        if (state.timeRemaining in 5..10) {
            if ((state.phase == GamePhase.GUESS && !state.hasSubmittedGuess) || (state.phase == GamePhase.VOTE && !state.didVote)) {
                child(Sound::class) {
                    attrs.soundName = "time_warn.mp3"
                    attrs.key = "time_warn"
                }
            }
        }

        if (state.timeRemaining in 1..4) {
            if ((state.phase == GamePhase.GUESS && !state.hasSubmittedGuess) || (state.phase == GamePhase.VOTE && !state.didVote)) {
                child(Sound::class) {
                    attrs.soundName = "time_warn_critical.mp3"
                    attrs.key = "time_warn_critical"
                }
            }
        }
    }

    override fun RBuilder.render() {
        if (state.didPlayerJustLeave) {
            child(Sound::class) {
                attrs.soundName = "leave.mp3"
            }
        }

        div("container") {
            div("header mb-5 ") {
                h1("animated bounceIn") {
                    span("text-primary") {
                        +gameName
                    }
                    +when (state.phase) {
                        GamePhase.NEED_NAME -> " – Welcome!"

                        GamePhase.NEED_GAME_ID -> " – Join a Lobby"
                        GamePhase.WAITING_FOR_NEXT_ROUND, GamePhase.WAITING_FOR_FIRST_GAME, GamePhase.GUESS, GamePhase.VOTE -> " – Lobby"
                    }
                }
                when (state.phase) {
                    GamePhase.WAITING_FOR_NEXT_ROUND, GamePhase.WAITING_FOR_FIRST_GAME, GamePhase.GUESS, GamePhase.VOTE -> {
                        h4("animated fadeInDown") {
                            +"#${state.lobby}"
                        }
                    }
                    else -> {
                    }
                }
            }

            when (state.socketState) {
                SocketState.AWAITING -> warningPanel("Connecting... Please wait.", "No connection could be established yet.", WarningPanelLevel.INFO)
                SocketState.CLOSED -> warningPanel("Connection to the server has been lost.", "Please reload the page. If the problem persists, please contact the game developer.", WarningPanelLevel.ERROR)
                SocketState.OPEN -> {
                }
            }

            if (state.phase == GamePhase.NEED_NAME) {
                div("nameform animated delay-qs fadeIn") {
                    h5("mb-3") {
                        +"What would you like to be called? 🤔️"
                    }
                    simpleInputField("Let's go!", state.namePlaceholder) {
                        val joined = it.replace(" ", "_")
                        println(joined)
                        socket.send("name $joined")
                    }
                }
            }

            if (state.phase == GamePhase.NEED_GAME_ID) {
                div("gameform") {
                    h5("mb-3") {
                        +"Please enter the Game ID. 🕹"
                    }
                    simpleInputField("Join") {
                            val joined = it.replace(" ", "_")
                        socket.send("game $joined")
                    }
                }
                div("gameform mt-3") {
                    inputButton("Generate a lobby ID", disabled = !state.interactAllowed) {
                        val id = listOf("red", "blue", "green", "big", "charlie", "alpha", "osu", "jk", "jalapeno", "rotor", "kilo", "metric")
                        val totalId = id.shuffled().take(4).joinToString("-")
                        socket.send("game $totalId")
                    }
                }
                if (state.availableLobbies.count() > 0) {
                    div("gameform mt-3") {
                        h5("mb-3") {
                            +"Or choose a lobby!"
                        }
                        child(VotingPanel::class) {
                            attrs {
                                options = state.availableLobbies.map { it.first to "#${it.first} (${it.second} Players)" }.toMap()
                                buttonPressHandler = {
                                    socket.send("game $it")
                                }
                                shouldEnable = true
                                shouldShow = true
                            }
                        }
                    }
                }
            }

            if (state.phase == GamePhase.WAITING_FOR_NEXT_ROUND || state.phase == GamePhase.WAITING_FOR_FIRST_GAME) {
                div("mb-5") {
                    inputButton("Start the round!", !state.interactAllowed) {
                        socket.send("start")
                    }
                }
            }

            if (state.phase == GamePhase.GUESS || state.phase == GamePhase.VOTE) {
                div("mb-3 animated zoomIn delay-qs") {
                    imageBox(state.image)
                }
                div {
                    val animation = if (state.timeRemaining < 4) {
                        "shake"
                    } else if (state.timeRemaining < 10) {
                        "pulse"
                    } else {
                        ""
                    }
                    h3("mb-3 ${if (state.timeRemaining < 10) "text-danger animated infinite $animation" else ""}") {
                        +"${state.timeRemaining}s remaining"
                    }
                }

            }

            if (state.phase == GamePhase.GUESS) {
                //todo: take inputs of field when nothing has been submitted so far.
                div("mb-5") {
                    simpleInputField(placeholder = "e.g. 'How to make cat lasagna'") {
                        setState {
                            hasSubmittedGuess = true
                        }
                        socket.send("guess $it")
                    }
                    if (state.hasSubmittedGuess) {
                        p("mt-2") {
                            +"(if you have had a better idea, you can still overwrite your guess.)"
                        }
                    }
                }
            }

            if (state.phase == GamePhase.VOTE) {
                h5("mb-2") {
                    +"Vote for your favorite caption!"
                }
            }
            if (state.phase == GamePhase.GUESS) {
                div("votes-container mt-4 mb-4") {
                    repeat(state.guesses.size - state.guesses.size) {
                        +"🤔"
                    }
                }
            }
            if (state.phase == GamePhase.VOTE) {
                div("votes-container mt-4 mb-4") {
                    repeat(state.votes) {
                        +"✅"
                    }
                    repeat(state.players.count() - state.votes) {
                        +"\uD83D\uDCA4"
                    }
                }

                repeat(state.votes) {
                    child(Sound::class) {
                        attrs.soundName = "vote.mp3"
                        attrs.key = "VoteSound-$it"
                    }
                }
            }
            if (state.phase == GamePhase.GUESS || state.phase == GamePhase.VOTE) {
                child(VotingPanel::class) {
                    attrs {
                        options = state.guesses
                        shouldEnable = state.canVote
                        shouldShow = state.phase == GamePhase.VOTE
                        thisPlayerId = state.thisPlayerId
                        buttonPressHandler = {
                            socket.send("vote $it")
                            setState {
                                canVote = false
                                didVote = true
                            }
                        }
                    }
                }
            }

            if (state.players.isNotEmpty()) {
                child(Leaderboard::class) {
                    attrs.players = state.players.map { x -> x.value.name to x.value.points }.toMap()
                }
            }

            if (state.phase == GamePhase.WAITING_FOR_NEXT_ROUND) {
                div("mt-4 mb-5") {
                    child(Evaluation::class) {
                        attrs.players = state.players.filterValues { it.lastRoundPoints != null }.map {
                            it.value.name to Pair(state.guesses[it.key], it.value.lastRoundPoints)
                        }.toMap()
                    }
                }
            }
        }

        if (state.phase == GamePhase.WAITING_FOR_FIRST_GAME || state.phase == GamePhase.WAITING_FOR_NEXT_ROUND) {
            h3("mb-3") {
                +"Current Game Mode: ${state.currentGameMode.humanDesc}"
            }
            h5("text-muted mb-4") {
                +state.currentGameMode.details
            }
            div("btn-group") {
                GameMode.values().map { gameMode ->
                    inputButton(gameMode.humanDesc, !state.interactAllowed) {
                        socket.send("GAMEMODE $gameMode")
                    }
                }
            }
        }

        if (state.phase == GamePhase.NEED_NAME) {
            div("mt-5 animated fadeIn delay-1s") {
                h3 {
                    +"What am I looking at?"
                }
                p {
                    b {
                        +gameName
                    }
                    +" is a multiplayer online game where you and your friends look at Reddit-curated images from the platform WikiHow and try to find funny captions for it. Once everyone has put in a suggestion, everyone gets to vote on the best caption! May the funniest person earn the most points!"
                }
            }
        }

        div {
            soundEffects()
        }

        about(state.totalNumberOfPlayers)
    }
}


inline fun Window.setMyInterval(timeout: Int, handler: dynamic): Int {
    return this.setInterval(handler, timeout)
}