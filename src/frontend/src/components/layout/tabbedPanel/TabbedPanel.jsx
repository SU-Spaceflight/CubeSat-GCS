import "./TabbedPanel.css"
import {useState} from "react";
import {Panel} from "../panel/Panel.jsx";

export function TabbedPanel({ tabs }) {
    const [active, setActive] = useState(tabs[0].id)
    return (
        <Panel headerContentLeft={
            <div className="tabs-bar">
                {tabs.map((t) => (
                    <button key={t.id} className={`tab ${t.id === active ? 'active' : ''}`} onClick={() => setActive(t.id)}>
                        {t.label}
                    </button>
                ))}
            </div>}>
            <div className="tab-content">
                {tabs.find((t) => t.id === active)?.content}
            </div>
        </Panel>
    )
}