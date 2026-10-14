import {useEffect, useState} from "react";
import "./Clock.css"

export function Clock() {
    const [now, setNow] = useState(() => new Date())

    useEffect(() => {
        const id = setInterval(() => setNow(new Date()), 250)
        return () => clearInterval(id)
    }, [])

    const date = now
        .toLocaleDateString('en-GB',
            { day: '2-digit', month: 'short', timeZone: 'UTC' }).toUpperCase();

    const time = now.toLocaleTimeString('en-GB',
        { hourCycle: 'h23', timeZone: 'UTC' });

    return (
        <div className="clock">
            <div className="clock-value">{date} | {time}</div>
        </div>
    )
}