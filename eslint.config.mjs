import eslint from "@eslint/js";
import tseslint from "typescript-eslint";
import { globalIgnores, defineConfig } from "eslint/config";

const rule = defineConfig(
    {
        files: ["*.ts", "*.tsx", "*.js", "*.jsx", "*.mjs", "*.mts", "*.cjs"],
        rules: {
            "eqeqeq": "off",
            "eol-last": ["error", "always"],
            "comma-spacing": ["error", {"before": false, "after": true}],
            "semi": ["error", "always"],
            "@typescript-eslint/no-unused-vars": "off",
            "@typescript-eslint/no-explicit-any": "off",
            "quotes": ["error", "double"]
        },
    },
);

export default tseslint.config(
    eslint.configs.recommended,
    tseslint.configs.recommended.map(
        it => {
            if (it.rules) it.rules["@typescript-eslint/ban-ts-comment"] = "off";
            return it;
        }
    ),
    globalIgnores(["**/build/**", "**/generated/**", "types.d.ts", "vite.*"], "Ignore generated directory"),
    rule
);
