package index

import react.RBuilder
import react.dom.a
import react.dom.div
import react.dom.p

fun RBuilder.About() {
    div("about") {
        p {
            + "Made by Sebi. Additional sound effects from "
            a {
                +"https://www.zapsplat.com"
                attrs {
                    href = "https://www.zapsplat.com"
                    rel = "nofollow noopener"
                }
            }
        }
    }
}