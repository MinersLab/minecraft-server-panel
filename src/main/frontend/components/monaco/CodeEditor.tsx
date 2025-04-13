import {ReactElement} from "react";
import {ReactAdapterElement, type RenderHooks} from "Frontend/generated/flow/ReactAdapter";
import Editor, {loader} from '@monaco-editor/react';
import * as monaco from "monaco-editor";

import logLanguage from "./language-log";
import mcspDarkTheme from "./theme-mcsp-dark";
import tomlLanguage from "./language-toml";
import {configureMonacoYaml} from "monaco-yaml";

loader.config(
    {
        "vs/nls": {
            availableLanguages: {
                "*": "zh-cn"
            }
        }
    }
);

export class McspCodeEditor extends ReactAdapterElement {

    override render(hooks: RenderHooks): ReactElement | null {
        const [value, setValue] = hooks.useState("value", "");
        const [fileName] = hooks.useState("fileName", "blank.log");
        const [height] = hooks.useState("height", "100%");
        const [width] = hooks.useState("width", "100%");
        const options: monaco.editor.IStandaloneEditorConstructionOptions = {
            selectOnLineNumbers: true,
        };
        return <Editor
            theme="mcsp-dark"
            height={height}
            width={width}
            value={value}
            options={options}
            path={fileName}
            onMount={(...args) => this.onMonacoMounted(...args)}
            onChange={(code) => code && setValue(code)}
        />
    }

    private onMonacoMounted(_editor: monaco.editor.IStandaloneCodeEditor, monacoInstance: typeof monaco) {
        logLanguage(monacoInstance);
        tomlLanguage(monacoInstance);
        mcspDarkTheme(monacoInstance);
        configureMonacoYaml(monacoInstance);
    }
}

customElements.define('mcsp-code-editor', McspCodeEditor);
