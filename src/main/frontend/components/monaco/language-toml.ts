/* eslint-disable no-useless-escape */
// noinspection RegExpRedundantEscape

/**
 * From https://github.com/microsoft/monaco-editor/blob/f7beb75f38c38b065d26f05760438d84224fdb1a/src/basic-languages/toml/toml.ts.
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
import * as monaco from "monaco-editor";
import {languages} from "monaco-editor";

export const conf: languages.LanguageConfiguration = {
    comments: {
        lineComment: '#'
    },
    brackets: [
        ['{', '}'],
        ['[', ']'],
        ['(', ')']
    ],
    autoClosingPairs: [
        {open: '{', close: '}'},
        {open: '[', close: ']'},
        {open: '(', close: ')'},
        {open: '"', close: '"'},
        {open: "'", close: "'"}
    ],
    folding: {
        offSide: true
    },
    onEnterRules: [
        {
            beforeText: /[\{\[]\s*$/,
            action: {
                indentAction: languages.IndentAction.Indent
            }
        }
    ]
};
export const language: languages.IMonarchLanguage = {
    tokenPostfix: '.toml',
    brackets: [
        {token: 'delimiter.bracket', open: '{', close: '}'},
        {token: 'delimiter.square', open: '[', close: ']'}
    ],
    numberInteger: /[+-]?(0|[1-9](_?[0-9])*)/,
    numberOctal: /0o[0-7](_?[0-7])*/,
    numberHex: /0x[0-9a-fA-F](_?[0-9a-fA-F])*/,
    numberBinary: /0b[01](_?[01])*/,
    floatFractionPart: /\.[0-9](_?[0-9])*/,
    floatExponentPart: /[eE][+-]?[0-9](_?[0-9])*/,
    date: /\d{4}-\d\d-\d\d/,
    time: /\d\d:\d\d:\d\d(\.\d+)?/,
    offset: /[+-]\d\d:\d\d/,
    escapes: /\\([btnfr"\\]|u[0-9a-fA-F]{4}|U[0-9a-fA-F]{8})/,
    identifier: /([\w-]+)/,
    identChainStart: /([\w-"'])/,
    valueStart: /(["'tf0-9+\-in\[\{])/,
    tokenizer: {
        root: [
            {include: '@comment'},
            {include: '@whitespace'},
            [/@identChainStart/, '@rematch', '@kvpair'],
            [/\[/, '@brackets', '@table'],
            [/=/, 'delimiter', '@value']
        ],
        comment: [[/#.*$/, 'comment']],
        whitespace: [[/[ \t\r\n]+/, 'white']],
        kvpair: [
            {include: '@whitespace'},
            {include: '@comment'},
            [/@identChainStart/, '@rematch', '@identChain.variable'],
            [
                /=/,
                {
                    token: 'delimiter',
                    switchTo: '@value'
                }
            ],
            [/./, '@rematch', '@pop']
        ],
        ...createIdentChainStates('variable'),
        table: [
            {include: '@whitespace'},
            {include: '@comment'},
            [/\[/, '@brackets', '@table'],
            [/@identChainStart/, '@rematch', '@identChain.type'],
            [/\]/, '@brackets', '@pop']
        ],
        ...createIdentChainStates('type'),
        value: [
            {include: '@whitespace'},
            {include: '@comment'},
            {include: '@value.cases'},
            [/./, '@rematch', '@pop']
        ],
        'value.string.singleQuoted': createSingleLineLiteralStringState('string.literal'),
        'value.string.doubleQuoted': createSingleLineStringState('string'),
        'value.string.multi.doubleQuoted': [
            [/[^"\\]+/, 'string.multi'],
            [/@escapes/, 'constant.character.escape'],
            [/\\$/, `constant.character.escape`],
            [/\\./, `constant.character.escape.invalid`],
            [/"""(""|")?/, 'string.multi', '@pop'],
            [/"/, 'string.multi']
        ],
        'value.string.multi.singleQuoted': [
            [/[^']+/, 'string.literal.multi'],
            [/'''(''|')?/, 'string.literal.multi', '@pop'],
            [/'/, 'string.literal.multi']
        ],
        'value.array': [
            {include: '@whitespace'},
            {include: '@comment'},
            [/\]/, '@brackets', '@pop'],
            [/,/, 'delimiter'],
            [/@valueStart/, '@rematch', '@value.array.entry'],
            [/.+(?=[,\]])/, 'source']
        ],
        'value.array.entry': [
            {include: '@whitespace'},
            {include: '@comment'},
            {include: '@value.cases'},
            [/.+(?=[,\]])/, 'source', '@pop'],
            [/./, 'source', '@pop']
        ],
        'value.inlinetable': [
            {include: '@whitespace'},
            {include: '@comment'},
            [/\}/, '@brackets', '@pop'],
            [/,/, 'delimiter'],
            [/@identChainStart/, '@rematch', '@value.inlinetable.entry'],
            [/=/, 'delimiter', '@value.inlinetable.value'],
            [/@valueStart/, '@rematch', '@value.inlinetable.value'],
            [/.+(?=[,\}])/, 'source', '@pop']
        ],
        'value.inlinetable.entry': [
            {include: '@whitespace'},
            {include: '@comment'},
            [/@identChainStart/, '@rematch', '@identChain.variable'],
            [
                /=/,
                {
                    token: 'delimiter',
                    switchTo: '@value.inlinetable.value'
                }
            ],
            [/.+(?=[,\}])/, 'source', '@pop']
        ],
        'value.inlinetable.value': [
            {include: '@whitespace'},
            {include: '@comment'},
            {include: '@value.cases'},
            [/.+(?=[,\}])/, 'source', '@pop'],
            [/./, 'source', '@pop']
        ],
        'value.cases': [
            [
                /"""/,
                {
                    token: 'string.multi',
                    switchTo: '@value.string.multi.doubleQuoted'
                }
            ],
            [/"(\\.|[^"])*$/, 'string.invalid'],
            [
                /"/,
                {
                    token: 'string',
                    switchTo: '@value.string.doubleQuoted'
                }
            ],
            [
                /'''/,
                {
                    token: 'string.literal.multi',
                    switchTo: '@value.string.multi.singleQuoted'
                }
            ],
            [/'[^']*$/, 'string.literal.invalid'],
            [
                /'/,
                {
                    token: 'string.literal',
                    switchTo: '@value.string.singleQuoted'
                }
            ],
            [/(true|false)/, 'constant.language.boolean', '@pop'],
            [
                /\[/,
                {
                    token: '@brackets',
                    switchTo: '@value.array'
                }
            ],
            [
                /\{/,
                {
                    token: '@brackets',
                    switchTo: '@value.inlinetable'
                }
            ],
            [/@numberInteger(?![0-9_oxbeE\.:-])/, 'number', '@pop'],
            [
                /@numberInteger(@floatFractionPart@floatExponentPart?|@floatExponentPart)/,
                'number.float',
                '@pop'
            ],
            [/@numberOctal/, 'number.octal', '@pop'],
            [/@numberHex/, 'number.hex', '@pop'],
            [/@numberBinary/, 'number.binary', '@pop'],
            [/[+-]?inf/, 'number.inf', '@pop'],
            [/[+-]?nan/, 'number.nan', '@pop'],
            [/@date[Tt ]@time(@offset|Z)?/, 'number.datetime', '@pop'],
            [/@date/, 'number.date', '@pop'],
            [/@time/, 'number.time', '@pop']
        ]
    }
};
type State = languages.IMonarchLanguageRule[];

function createIdentChainStates(tokenClass: string): Record<string, State> {
    const singleQuotedState = `identChain.${tokenClass}.singleQuoted`;
    const singleQuoteClass = `${tokenClass}.string.literal`;
    const doubleQuotedState = `identChain.${tokenClass}.doubleQuoted`;
    const doubleQuoteClass = `${tokenClass}.string`;
    return {
        [`identChain.${tokenClass}`]: [
            {include: '@whitespace'},
            {include: '@comment'},
            [/@identifier/, tokenClass],
            [/\./, 'delimiter'],
            [/'[^']*$/, `${tokenClass}.invalid`],
            [
                /'/,
                {
                    token: singleQuoteClass,
                    next: `@${singleQuotedState}`
                }
            ],
            [/"(\\.|[^"])*$/, `${tokenClass}.invalid`],
            [
                /"/,
                {
                    token: doubleQuoteClass,
                    next: `@${doubleQuotedState}`
                }
            ],
            [/./, '@rematch', '@pop']
        ],
        [singleQuotedState]: createSingleLineLiteralStringState(singleQuoteClass),
        [doubleQuotedState]: createSingleLineStringState(doubleQuoteClass)
    };
}

function createSingleLineLiteralStringState(tokenClass: string): State {
    return [
        [/[^']+/, tokenClass],
        [/'/, tokenClass, '@pop']
    ];
}

function createSingleLineStringState(tokenClass: string): State {
    return [
        [/[^"\\]+/, tokenClass],
        [/@escapes/, 'constant.character.escape'],
        [/\\./, `constant.character.escape.invalid`],
        [/"/, tokenClass, '@pop']
    ];
}

export default function register(thisMonaco: typeof monaco) {
    thisMonaco.languages.register({"id": "toml", extensions: [".toml"]})
    thisMonaco.languages.setMonarchTokensProvider("toml", language)
}