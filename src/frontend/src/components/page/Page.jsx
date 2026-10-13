import "./Page.css"

export default function Page(props) {
    const {title} = props;
    return (
        <div className="page">
            <div className="page-header">
                <h1>{title}</h1>

            </div>
            <div className="page-content">
                    {props.children}
            </div>
            <div className="page-footer">

            </div>
        </div>
    );
}