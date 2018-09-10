package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

interface EvaluationProps : RProps {
    var players: Map<String, Pair<String?, Int?>> //Playername, <Phrase, Points>
}

class Evaluation(props: EvaluationProps) : RComponent<EvaluationProps, RState>(props) {
    override fun RBuilder.render() {
        val sorted = props.players.toList().sortedByDescending { it.second.second }.toMap()

        div("evaluation") {
            val players = sorted.count { !(it.value.first == null || it.value.second == null) }
            if (players > 0) {
                sorted.map {
                    if (it.value.first == null || it.value.second == null) {

                    } else {
                        div("card mt-3 p-1 animated jackInTheBox") {
                            h5("card-title, text-primary") {
                                +it.key
                            }
                            h3("card-title") {
                                +"${it.value.second} "
                                i("fas fa-coins") {}
                            }
                            p("text-info") {
                                +"${it.value.first}"
                            }
                        }
                    }
                }
            } else {
                p {
                    +"Hmm, looks like nobody scored this round..."
                }
            }

        }
    }
}