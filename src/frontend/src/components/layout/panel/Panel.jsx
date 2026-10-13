import "./Panel.css"
import {Spacer} from "../spacer/Spacer.jsx";

export function Panel(props) {
    const {title, headerContentLeft, headerContentRight} = props;
    return (
        <div className="panel">
            {(title || headerContentLeft || headerContentRight) && (
                <div className="panel-head">
                    {(title) && (
                        <span className="panelTitle">{title}</span>
                    )}
                    {headerContentLeft}
                    <Spacer />
                    {headerContentRight}
                </div>
            )}
            <div className="panel-body">
                {props.children}
            </div>
        </div>
    )
}