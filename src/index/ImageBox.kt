package index

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.img

interface ImageBoxProps : RProps {
    var image: String
}

class ImageBox(props: ImageBoxProps) : RComponent<ImageBoxProps, RState>(props) {
    override fun RBuilder.render() {

        img(src = props.image, classes = "rounded img-fluid") {}
    }
}