---
name: material-icons
description: Find, select, and integrate official Google Material Symbols and Material Icons. Use when a user asks to add, replace, search for, export, or choose UI icons; when icon filters such as meaning, category, style, fill, weight, grade, optical size, platform, or output format are specified; or when implementing icons in Android Compose, web, React, Vue, Flutter, SVG, or another app project.
---

# Material Icons

Use official Material Symbols by default. Use classic Material Icons only when the project already uses them, requires two-tone, or explicitly asks for them.

## Workflow

1. Detect the target environment before choosing an integration path. Reuse existing local-asset conventions and icon style.
2. Extract requested filters. Ask one concise question only if the intended action is ambiguous. Infer `outlined`, 24 px, and accessible labels when omitted.
3. Return or use 2–4 candidates with exact Material Symbol names. Prefer the most literal, familiar action over a metaphor.
4. Integrate only the selected icon. Do not add a global font or an entire icon bundle when a single vector asset is sufficient.
5. Add an accessible label for interactive icons; use a null/decorative label only where the icon is explicitly redundant to adjacent text.
6. Read `material-icons-policy.json` at the repository root before asking configuration questions. Never overwrite it without explicit permission.
7. Detect the user’s language and use it exclusively in questions, suggestions, options, and answers.

## Selection

Accept `meaning`, `platform`, `set`, `style`, `fill`, `weight`, `grade`, `opticalSize`, `size`, `color`, `format`, and `accessibilityLabel`.

- Default set: Material Symbols; use Material Icons only when required.
- Default style: outlined; alternatives: rounded or sharp.
- Use filled states sparingly to indicate selection or a saved state.
- Default axes: fill 0, weight 400, grade 0, optical size 24.
- Prefer 20 or 24 px when pixel-grid crispness matters.
- Never use Material Symbols for third-party brand logos; use approved brand assets.

For bottom navigation, tabs, and comparable persistent navigation, preserve the same symbol metaphor in selected and unselected states. Prefer the same Material Symbol with `FILL` 0/1 where the platform supports it.

## Android Compose

This project is Android Compose. Prefer official Material Symbol SVGs imported as local VectorDrawable resources instead of adding or expanding `androidx.compose.material.icons`.

1. Use the chosen official 24 px symbol in outlined, rounded, or sharp style.
2. Import it as `res/drawable/ic_<name>.xml` and preserve its viewport and path data.
3. Render it with `Icon(painterResource(R.drawable.ic_<name>), contentDescription = "…")`.
4. Use `contentDescription = null` only for genuinely decorative icons.
5. Keep drawable names lowercase with underscores and do not rename existing resources without updating all references.
6. Preserve tint, size, modifiers, click behavior, and selected-state semantics.

When an exact Material Symbol cannot be established, do not invent a name. Suggest the closest verified alternatives or, only when the user asked to proceed, use the least misleading option and add a concise `TODO(material-icons)` note at the use site.

## Reporting

For each integration, state the exact symbol name, set/style, and output path. Material Symbols are Apache-2.0 licensed.
