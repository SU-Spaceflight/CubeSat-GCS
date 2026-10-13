export function Stack(props) {
    const { gap = 8, align, style } = props;
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap, alignItems: align, ...style }}>
            {props.children}
        </div>
    );
}