package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.li
import react.dom.ul

interface EvaluationProps: RProps {
    var players: Map<String, Pair<String?, Int?>>
}

class Evaluation(props: EvaluationProps): RComponent<EvaluationProps, RState>(props) {
    override fun RBuilder.render() {
        div("evaluation") {
            ul {
                props.players.map {
                    if(it.value.first == null || it.value.second == null) {

                    } else {
                        li {
                            +"${it.key}: \"${it.value.first}\": +${it.value.second} Points"
                        }
                    }
                }
            }
        }
    }
}