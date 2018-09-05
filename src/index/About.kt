package index

import react.RBuilder
import react.dom.a
import react.dom.footer

fun RBuilder.about(numberOfPlayers: Int) {
    footer {
        +"Made by Sebastian with ❤️ and 🥃. Additional sound effects from "
        a("https://www.zapsplat.com") {
            +"zapsplat.com"
            attrs { rel = "nofollow noopener" }
        }
        +" Frontend: React/Kotlin using "
        a("https://github.com/JetBrains/create-react-kotlin-app", classes = "nobreak") {
            +"create-react-kotlin-app"
            attrs { rel = "nofollow noopener" }
        }
        +". Backend: Kotlin "
        a("http://ktor.io/") {
            +"Ktor"
            attrs { rel = "nofollow noopener" }
        }
        +". Hosted on Heroku."
        if (numberOfPlayers > 0) {
            +" Serving a total of $numberOfPlayers players!"
        }
    }
}