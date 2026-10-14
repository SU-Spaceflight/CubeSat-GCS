import Page from "../components/page/Page.jsx";
import {Panel} from "../components/layout/panel/Panel.jsx";
import {Stack} from "../components/layout/stack/Stack.jsx";
import {Grid} from "../components/layout/grid/Grid.jsx";
import {TabbedPanel} from "../components/layout/tabbedPanel/TabbedPanel.jsx";

export default function Overview() {
  return (
    <Page title={"Overview"}>
      <Stack gap={16}>
        <TabbedPanel tabs={[
          {
            id: 3,
            label: "Hello",
            content: "<p>hello</p>"
          },
          {
            id: 4,
            label: "Hello2",
            content: "<p>hello2</p>"
          },
          {
            id: 5,
            label: "Hello3",
            content: "<p>hello2</p>"
          }
        ]}/>
        <Panel title="test1">
          <Grid cols={4}>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
          </Grid>
        </Panel>
        <Panel title="test1">
          <Grid cols={4}>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
          </Grid>
        </Panel>
        <Panel title="test1">
          <Grid cols={4}>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
          </Grid>
        </Panel>
        <Panel title="test1">
          <Grid cols={4}>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
            <p>hello</p>
          </Grid>
        </Panel>
        <Grid cols={2}>
          <Panel title="test2">
            <p>hello2</p>
          </Panel>
          <Panel title="test3">
          </Panel>
        </Grid>
      </Stack>
    </Page>
  )
}
