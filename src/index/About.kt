package index

import kotlinx.html.style
import react.RBuilder
import react.dom.a
import react.dom.div
import react.dom.jsStyle
import react.dom.p

fun RBuilder.About() {
    div("about") {
        p {
            + "Made by Sebastian with ❤️ and 🥃."}
        p {
            +"Additional sound effects from "
            a {
                +"https://www.zapsplat.com"
                attrs {
                    href = "https://www.zapsplat.com"
                    rel = "nofollow noopener"
                }
            }
        }
        p {
            +"Frontend: React/Kotlin using "
            a(classes = "nobreak") {
                +"create-react-kotlin-app"
                attrs {
                    href = "https://github.com/JetBrains/create-react-kotlin-app"
                    rel = "nofollow noopener"
                }
            }
        }
        p {
            +"Backend: Kotlin "
            a {
                +"Ktor"
                attrs {
                    href = "http://ktor.io/"
                    rel = "nofollow noopener"
                }
            }
        }
        p {
            +"Hosted on Heroku."
        }
    }
}