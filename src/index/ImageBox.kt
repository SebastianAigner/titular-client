package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.img

interface ImageBoxProps: RProps {
    var image: String
}

class ImageBox(props: ImageBoxProps): RComponent<ImageBoxProps, RState>(props) {
    override fun RBuilder.render() {
        div("imagebox") {
            img(src=props.image) {

            }
        }
    }
}