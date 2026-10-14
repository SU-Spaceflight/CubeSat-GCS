import { useEffect, useState } from "react";

export default function VideoFeed({ src = "/api/video/stream"}) {
    const [error, setError] = useState(false);

    useEffect(() => {
        if (!error) return;

        const t = setTimeout(() => {
            setError(false);
        }, 2000);

        return () => clearTimeout(t);

    }, [error]);

    return (
        <div className="video-feed-wrapper">
            <img src={`${src}`} className="video-feed"/>
        </div>
    );
}