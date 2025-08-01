import {ReactAdapterElement, type RenderHooks} from "Frontend/generated/flow/ReactAdapter";
import {ReactElement, useEffect} from "react";
import ReactECharts, {EChartsOption} from "echarts-for-react";
import {theme} from "../../Chart";

function processData(data: number[]): EChartsOption {
    return {
        title: {
            show: false,
            text: "接口请求数量"
        },
        xAxis: {
            type: "category",
            data: new Array(data.length).fill(null).map((_, index) => 59 - index * 3 + "s")
        },
        yAxis: {
            type: "value"
        },
        series: [
            {
                data: data,
                type: "line",
                smooth: false
            }
        ]
    };
}

const getData = () => fetch("/api/visual-data/request-count")
    .then(r => r.json())
    .then(processData);

class McspRequestCountingChart extends ReactAdapterElement {

    override render(hooks: RenderHooks): ReactElement | null {
        const [options, setOptions] = hooks.useState<EChartsOption>("options", processData(new Array(20).fill(0)));

        useEffect(() => {
            const interval = setInterval(
                () => getData().then(setOptions),
                3000
            );
            return () => clearInterval(interval);
        });
        return <ReactECharts option={options} theme={theme} notMerge={true} lazyUpdate={true}></ReactECharts>;
    }

}

customElements.define("mcsp-home-request-counting-chart", McspRequestCountingChart);
