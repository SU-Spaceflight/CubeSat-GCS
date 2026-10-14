import ScrollArea from "../layout/scrollArea/ScrollArea.jsx";
import {Spacer} from "../layout/spacer/Spacer.jsx";
import "./Page.css"
import {Clock} from "../clock/Clock.jsx";

export default function Page(props) {
    const {title, actions} = props;
    return (
        <div className="page">
            <div className="page-header">
                <h1>{title}</h1>
                <Spacer />
                {actions}
                <Spacer />
                <Clock epoch="2026-03-01T12:00:00Z" />
            </div>
            <ScrollArea scrollX={false} scrollY={true}>
                <div className="page-content">
                    {props.children}
                </div>
            </ScrollArea>
            <div className="page-footer">

            </div>
        </div>
    );
}