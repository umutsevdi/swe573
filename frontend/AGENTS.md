# Agent Guidelines for SWE573 Frontend

## Commands

- **Dev server**: `npm run dev`
- **Build**: `npm run build`
- **Type check**: `npm run check`
- **Lint**: `npm run lint`
- **Format**: `npm run format`

## Code Style

- **Formatting**: Prettier (tabs, single quotes, no trailing commas, 100 char width)
- **TypeScript**: Strict mode, explicit types for component props
- **Imports**: ES6 imports, use `$lib/` alias for internal modules
- **Naming**: PascalCase components, camelCase variables/functions
- **Styling**: Tailwind CSS + DaisyUI components
- **Error handling**: Throw errors for invalid states, validate inputs
- **Comments**: Minimal, only when necessary for clarity

## Guidelines

- Strictly use **DaisyUI** and **Lucide** components. Do not use Tailwind CSS
  unless it's necessary.
