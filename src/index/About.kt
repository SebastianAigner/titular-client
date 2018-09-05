package index

import react.RBuilder
import react.dom.a
import react.dom.footer

fun RBuilder.About(numberOfPlayers: Int) {
    footer {

        +"Made by Sebastian with ❤️ and 🥃."

        +" Additional sound effects from "
        a {
            +"https://www.zapsplat.com"
            attrs {
                href = "https://www.zapsplat.com"
                rel = "nofollow noopener"
            }
        }
        +" Frontend: React/Kotlin using "
        a(classes = "nobreak") {
            +"create-react-kotlin-app"
            attrs {
                href = "https://github.com/JetBrains/create-react-kotlin-app"
                rel = "nofollow noopener"
            }
        }
        +" Backend: Kotlin "
        a {
            +"Ktor"
            attrs {
                href = "http://ktor.io/"
                rel = "nofollow noopener"
            }
        }
        +" Hosted on Heroku."
        if (numberOfPlayers > 0) {
            +" Serving a total of $numberOfPlayers players!"
        }
    }
}