import * as monaco from "monaco-editor";

export const language: monaco.languages.IMonarchLanguage = {
    defaultToken: "text",
    tokenizer: {
        root: [
            // Verbose levels
            [/\b(Trace)\b:/, 'comment.log.verbose'],
            [/\[(verbose|verb|vrb|vb|v)]/i, 'comment.log.verbose'],
            [/(?<=^[\s\d]*)\bV\b/, 'comment.log.verbose'],

            // Debug levels
            [/\b(DEBUG|Debug)\b|\bdebug:/i, 'markup.changed.log.debug'],
            [/\[(debug|dbug|dbg|de|d)]/i, 'markup.changed.log.debug'],
            [/(?<=^[\s\d]*)\bD\b/, 'markup.changed.log.debug'],

            // Info levels
            [/\b(HINT|INFO|INFORMATION|Info|NOTICE|II)\b|\b(?:info|information):/i, 'markup.inserted.log.info'],
            [/\[(information|info|inf|in|i)]/i, 'markup.inserted.log.info'],
            [/(?<=^[\s\d]*)\bI\b/, 'markup.inserted.log.info'],

            // Warning levels
            [/\b(WARNING|WARN|Warn|WW)\b|\bwarning:/i, 'markup.deleted.log.warning'],
            [/\[(warning|warn|wrn|wn|w)]/i, 'markup.deleted.log.warning'],
            [/(?<=^[\s\d]*)\bW\b/, 'markup.deleted.log.warning'],

            // Error levels
            [/\b(ALERT|CRITICAL|EMERGENCY|ERROR|FAILURE|FAIL|Fatal|FATAL|Error|EE)\b|\berror:/i, 'string.regexp.strong.log.error'],
            [/\[(error|eror|err|er|e|fatal|fatl|ftl|fa|f)]/i, 'string.regexp.strong.log.error'],
            [/(?<=^[\s\d]*)\bE\b/, 'string.regexp.strong.log.error'],

            // Date formats
            [/\b\d{4}-\d{2}-\d{2}(?=T|\b)/, 'comment.log.date'],
            [/(?<=^|\s)\d{2}[^\w\s]\d{2}[^\w\s]\d{4}\b/, 'comment.log.date'],
            [/T?\d{1,2}:\d{2}(:\d{2}([.,]\d+)?)?(Z| ?[+-]\d{1,2}:\d{2})?\b/, 'comment.log.date'],
            [/T\d{4}([.,]\d+)?(Z| ?[+-]\d{3,4})?\b/, 'comment.log.date'],

            // Hashes and identifiers
            [/\b([0-9a-fA-F]{40}|[0-9a-fA-F]{10}|[0-9a-fA-F]{7})\b/, 'constant.language'],
            [/\b[0-9a-fA-F]{8}-?([0-9a-fA-F]{4}-?){3}[0-9a-fA-F]{12}\b/, 'constant.language.log.constant'],
            [/\b([0-9a-fA-F]{2,}[:-])+[0-9a-fA-F]{2,}\b/, 'constant.language.log.constant'],
            [/\b(0x[a-fA-F0-9]+)\b/, 'constant.language.log.constant'],
            [/\b([0-9]+|true|false|null)\b/, 'constant.language.log.constant'],

            // Strings
            [/"([^"\\]|\\.)*"/, 'string.log.string'],
            [/'([^'\\]|\\.)*'/, 'string.log.string'],

            // Exceptions
            [/\b([A-Za-z.]*Exception)\b/, 'string.regexp.emphasis.log.exceptiontype'],
            [/^[\t ]*at[\t ].*$/, {token: 'string.key.emphasis.log.exception'}],

            // URLs and domains
            [/\b[a-z]+:\/\/\S+\b\/?/, 'constant.language.log.constant'],
            [/(?<![\w/\\])([\w-]+\.)+[\w-]+(?<![\w/\\])/, 'constant.language.log.constant']
        ]
    }
};

export default function register(thisMonaco: typeof monaco) {
    thisMonaco.languages.register({id: 'log', extensions: [".log"]});
    thisMonaco.languages.setMonarchTokensProvider('log', language);
}
