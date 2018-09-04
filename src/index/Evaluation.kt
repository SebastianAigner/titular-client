package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.li
import react.dom.p
import react.dom.ul

interface EvaluationProps: RProps {
    var players: Map<String, Pair<String?, Int?>> //Playername, <Phrase, Points>
}

class Evaluation(props: EvaluationProps): RComponent<EvaluationProps, RState>(props) {
    override fun RBuilder.render() {
        div("evaluation") {
            div {
                val players = props.players.count { !(it.value.first == null || it.value.second == null) }
                if(players > 0) {
                    props.players.map {
                        if (it.value.first == null || it.value.second == null) {

                        } else {
                            p {
                                +"${it.key}: \"${it.value.first}\": +${it.value.second} Points"
                            }
                        }
                    }
                }
                else {
                    p {
                        +"Hmm, looks like nobody scored this round..."
                    }
                }
            }
        }
    }
}