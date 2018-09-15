package index

import react.*
import react.dom.*
import kotlin.browser.window

interface EvaluationProps : RProps {
    var players: Map<String, Pair<String?, Int?>> //Playername, <Phrase, Points>
}

interface EvaluationState : RState {
    var currentText: String
    var shown: Int
    var timerId: Int
}


class Evaluation(props: EvaluationProps) : RComponent<EvaluationProps, EvaluationState>(props) {

    override fun EvaluationState.init(props: EvaluationProps) {
        shown = 0
        timerId = window.setMyInterval(1000) {
            updatePopIn()
        }
    }

    fun updatePopIn() {
        setState {
            shown += 1
        }
        if (state.shown >= props.players.count()) {
            window.clearInterval(state.timerId)
        }
    }


    override fun RBuilder.render() {
        val sorted = props.players.toList().sortedByDescending { it.second.second }.toMap()
        println("The state is ${state.currentText}")
        div("evaluation") {
            val players = sorted.count { !(it.value.first == null || it.value.second == null) }
            if (players > 0) {
                sorted.toList().take(state.shown).toMap().map {
                    // yay for memory allocations
                    if (it.value.first == null || it.value.second == null) {

                    } else {
                        evalCard(it.key, it.value.second!!, it.value.first!!)
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

fun RBuilder.evalCard(name: String, points: Int, sentence: String) {
    div("card mt-3 p-1 animated jackInTheBox") {
        h5("card-title, text-primary") {
            +name
        }
        h3("card-title animated flipInY delay-1s") {
            +"+"
            +points.toString()
            +" "
            i("fas fa-coins") {}
        }
        p("text-info") {
            +sentence
        }
    }
}