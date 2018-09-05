package index

import kotlinext.js.require
import kotlinext.js.requireAll
import org.w3c.dom.CloseEvent
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import react.*
import react.dom.*
import kotlin.browser.document
import kotlin.browser.window

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

enum class GameMode(val humanDesc: String, val details: String) {
    TOP_ALL_TIME("All Time", "Get a selection of the all-time best WikiHow illustrations, curated by the Reddit Community on /r/disneyvacation."),
    TOP_THIS_MONTH("Month", "Played through the all-time best? The monthly top content provides great illustrations that are fresh on a more frequent basis."),
    TOP_THIS_WEEK("Week", "The weekly top - not as curated as monthly or all time, but fresh!"),
    HOT("Hot (today)", "Cutting-edge illustrations straight from the tap!"),
    HMMM("Bonus: Hmmm", "Images that really make you go 🤔. Courtesy of /r/hmmm.")
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
    var didPlayerJustLeave: Boolean
    var hasSubmittedGuess: Boolean
    var votes: Int
    var currentGameMode: GameMode
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
        hasSubmittedGuess = false
        votes = 0
        currentGameMode = GameMode.TOP_ALL_TIME
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
                    "gamemode" -> {
                        setState {
                            currentGameMode = GameMode.valueOf(str[1])
                        }
                    }
                    "startround" -> {
                        setState {
                            guesses = mapOf()
                            phase = GamePhase.GUESS
                            hasSubmittedGuess = false
                            players.values.forEach { it.lastRoundPoints = null }
                            votes = 0
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
                            timeRemaining = 0
                        }
                    }
                    "vote_indicator" -> {
                        setState {
                            votes++
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

                    "player_leave" -> {
                        setState {
                            players = players.filterNot { pl -> pl.key == str[1] }
                            didPlayerJustLeave = true
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

    fun RBuilder.SoundEffects() {
        when (state.phase) {

            GamePhase.NEED_NAME -> {
            }
            GamePhase.NEED_GAME_ID -> {
            }
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
        if (state.timeRemaining in 5..10) {
            child(Sound::class) {
                attrs.soundName = "time_warn.mp3"
                attrs.key = "time_warn"
            }
        }
        if (state.timeRemaining in 1..4) {
            child(Sound::class) {
                attrs.soundName = "time_warn_critical.mp3"
                attrs.key = "time_warn_critical"
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
            div("header") {
                h1("mb-5") {
                    span("text-primary") {
                        +gameName
                    }
                    +when (state.phase) {
                        GamePhase.NEED_NAME -> {
                            " – Welcome!"
                        }

                        GamePhase.NEED_GAME_ID -> {
                            " – Join a Lobby"
                        }
                        GamePhase.WAITING_FOR_NEXT_ROUND, GamePhase.WAITING_FOR_FIRST_GAME -> {
                            " – Lobby #${state.lobby}"
                        }
                        else -> {
                            ""
                        }
                    }
                }
            }

            when (state.socketState) {
                SocketState.AWAITING -> {
                    WarningPanel("Connecting... Please wait.", "No connection could be established yet.", WarningPanelLevel.INFO)
                }
                SocketState.CLOSED -> {
                    WarningPanel("Connection to the server has been lost.", "Please reload the page. If the problem persists, please contact the game developer.", WarningPanelLevel.ERROR)
                }
            }

            if (state.phase == GamePhase.NEED_NAME) {
                div("nameform") {
                    h5("mb-3") {
                        +"What would you like to be called? 🤔️"
                    }
                    child(SimpleInputField::class) {
                        attrs.callToAction = "Let's go!"
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

            if (state.phase == GamePhase.NEED_GAME_ID) {
                div("gameform") {
                    h5("mb-3") {
                        +"Please enter the Game ID. 🕹"
                    }
                    child(SimpleInputField::class) {
                        attrs.callToAction = "Join"
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

            if (state.phase == GamePhase.WAITING_FOR_NEXT_ROUND || state.phase == GamePhase.WAITING_FOR_FIRST_GAME) {
                div("mb-5") {
                    child(Button::class) {
                        attrs.label = "Start the round!"
                        attrs.handleClick = {
                            state.socket.send("start")
                        }
                    }
                }
            }

            if (state.phase == GamePhase.GUESS || state.phase == GamePhase.VOTE) {
                child(ImageBox::class) {
                    attrs.image = state.image
                }
                h3 {
                    +"${state.timeRemaining}s remaining"
                }
            }

            if (state.phase == GamePhase.GUESS) {
                //todo: take inputs of field when nothing has been submitted so far.
                child(SimpleInputField::class) {
                    attrs.placeholder = "e.g. 'How to make cat lasagna'"
                    attrs.key = "Guess-Input"
                    attrs.handleNameAdd = {
                        setState {
                            hasSubmittedGuess = true
                        }
                        state.socket.send("guess $it")
                    }
                }
                if (state.hasSubmittedGuess) {
                    p {
                        +"(if you have had a better idea, you can still overwrite your guess.)"
                    }
                }
            }

            if (state.phase == GamePhase.VOTE) {
                h5("mb-2") {
                    +"Vote for your favorite caption!"
                }
            }
            if (state.phase == GamePhase.GUESS) {
                h5 {
                    +"taking suggestions..."
                }
            }
            if (state.phase == GamePhase.GUESS || state.phase == GamePhase.VOTE) {

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

            if (state.phase == GamePhase.VOTE) {
                div("votes-container mt-5") {
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

            if (state.players.size > 0) {
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
            /*
            child(Evaluation::class) {
                attrs.players = mapOf("Johann" to Pair("Phrasendrescher", 17), "Margret" to Pair("A long and funny sentence. Hahahahaha", 7))
            }
            */
        }

        //sounds for each phase

        if (state.phase == GamePhase.WAITING_FOR_FIRST_GAME || state.phase == GamePhase.WAITING_FOR_NEXT_ROUND) {
            h3("mb-3") {
                +"Current Game Mode: ${state.currentGameMode.humanDesc}"
            }
            h5("text-muted mb-4") {
                +state.currentGameMode.details
            }
            div("btn-group") {
                GameMode.values().map { gameMode ->
                    child(Button::class) {
                        attrs.label = gameMode.humanDesc
                        attrs.handleClick = {
                            state.socket.send("GAMEMODE $gameMode")
                        }
                    }
                }
            }
        }


        if (state.phase == GamePhase.NEED_NAME) {
            div("mt-5") {
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
            SoundEffects()
        }

        About()
    }
}

