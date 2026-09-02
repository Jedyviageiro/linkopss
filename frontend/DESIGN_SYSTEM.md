# LinkOps design system

These are core UI rules. New components and screens must use the tokens in
`src/assets/styles/main.css`; do not introduce one-off brand, status, neutral,
font-family, or typography values.

## Color

- Primary: LinkOps Green `#16A34A`, Deep Navy `#0F172A`, Soft Background
  `#F8FAFC`, and White `#FFFFFF`.
- Secondary: Emerald `#22C55E`, Lime `#84CC16`, Amber `#F59E0B`, and Indigo
  `#6366F1`.
- Neutral: Slate 900 `#1E293B`, Slate 700 `#334155`, Slate 500 `#64748B`,
  Slate 300 `#CBD5E1`, and Slate 200 `#E2E8F0`.
- Green identifies LinkOps and primary actions. Secondary colors communicate
  meaning or status. Neutrals build interface structure.
- Use Deep Navy for headings and strong text, Soft Background for application
  backgrounds, and White for cards, fields, and other surfaces.

## Typography

Inter is the only application font family.

| Role | Size / line height | Weight |
| --- | --- | --- |
| H1 | 56px / 64px | 700 |
| H2 | 36px / 44px | 700 |
| H3 | 28px / 36px | 600 |
| H4 | 22px / 28px | 600 |
| H5 | 18px / 24px | 500 |
| Body large | 16px / 26px | 400 |
| Body base | 15px / 24px | 400 |
| Body small | 14px / 20px | 400 |
| Caption | 12px / 16px | 400 |

Normal fields and placeholders use 15px / 24px at weight 400. Field labels
use 14px / 20px at weight 500. Helper copy uses 12px / 16px at weight 400,
and errors use the same size at weight 500. Placeholders must never be smaller
than entered text; use Slate 500 to distinguish them.

Default buttons use 15px / 20px at weight 600. Large buttons use 16px / 24px,
and small buttons use 14px / 20px, both at weight 600. Button labels use
sentence case and primary actions must never use regular weight.

## Spacing

Use a 4px base spacing scale. Prefer 4, 8, 12, 16, 20, 24, and 32px values;
do not introduce arbitrary visual gaps. Use 4px between a field label and its
control, 12px between repeated fields, 16px between form sections, and 12px
around dividers. Keep the same spacing for repeated controls in a form.
