import "./ScrollArea.css"

export default function ScrollArea(props) {
    const { scrollX, scrollY } = props;

    let scrollAreaClass = "scroll-area";

    if (scrollX === true) {
        scrollAreaClass += " scroll-x";
    }
    if (scrollY === true) {
        scrollAreaClass += " scroll-y"
    }

    return (
        <div className="scroll-wrapper">
            <div className={scrollAreaClass}>
                {props.children}
            </div>
        </div>
    );
}