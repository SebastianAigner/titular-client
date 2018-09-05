package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.audio

interface SoundState : RState

interface SoundProps : RProps {
    var soundName: String
}

class Sound(props: SoundProps) : RComponent<SoundProps, SoundState>(props) {
    override fun SoundState.init(props: SoundProps) {}

    override fun RBuilder.render() {
        audio {
            attrs {
                src = "/sounds/${props.soundName}"
                autoPlay = true
            }
        }
    }
}