import {ReactElement, useEffect} from "react";
import {ReactAdapterElement, type RenderHooks} from "Frontend/generated/flow/ReactAdapter";

export class McspInterval extends ReactAdapterElement {

    override render(hooks: RenderHooks): ReactElement | null {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const [timeout, _] = hooks.useState<number>("timeout");
        const [times, setTimes] = hooks.useState<number>("times", 0);
        useEffect(() => {
            const id = setInterval(() => setTimes(times + 1), timeout);
            return () => clearInterval(id);
        });
        return <></>;
    }

}

customElements.define('mcsp-util-interval', McspInterval);
