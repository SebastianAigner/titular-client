package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h4
import react.dom.h6
import react.dom.i

interface LeaderboardProps: RProps {
    var players: Map<String, Int>
}

class Leaderboard(props: LeaderboardProps): RComponent<LeaderboardProps, RState>(props) {
    override fun RBuilder.render() {
        val leaders = props.players.toList().sortedByDescending { it.second }.toMap()
        div("leaderboard") {
            leaders.map {
                div("card mb-1 pr-1 pt-1") {
                    div("card-body") {
                        h4("card-title") {
                            +it.key
                        }
                        h6("card-subtitle text-info rightalign") {
                            +"${it.value} "
                            i("fas fa-coins") {}
                        }
                    }
                }
            }

        }
    }
}