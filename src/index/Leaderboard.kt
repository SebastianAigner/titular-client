package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.li
import react.dom.p
import react.dom.ul

interface LeaderboardProps: RProps {
    var players: Map<String, Int>
}

class Leaderboard(props: LeaderboardProps): RComponent<LeaderboardProps, RState>(props) {
    override fun RBuilder.render() {
        div("leaderboard") {
                props.players.map {
                    p {
                        +"${it.key}: ${it.value} Points"
                    }
                    if(props.players.size > 1) {
                        child(Sound::class) {
                            attrs.soundName = "pop.mp3"
                        }
                    }
            }
        }
    }
}