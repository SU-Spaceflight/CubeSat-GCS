export function Grid(props) {
    const { cols = 3, gap = 12, style } = props

    return (
        <div style={{ display: 'grid', gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))`, gap, ...style }}>
            {props.children}
        </div>
    );
}