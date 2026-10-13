export function Row(props) {
    const { gap = 8, align = 'center', justify, wrap = false, style } = props;
    return (
        <div style={{ display: 'flex', gap, alignItems: align, justifyContent: justify, flexWrap: wrap ? 'wrap' : 'nowrap', ...style }}>
            {props.children}
        </div>
    )
}