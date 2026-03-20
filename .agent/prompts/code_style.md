---
description: Code Style and Framework Conventions
---

# Code Style Guidelines

## Backend (Java 21 & Micronaut)
- **Records**: Use Java 21 `record`s for all DTOs and Data Models to reduce boilerplate unless mutability is strictly required. Example: `public record DashboardDTO(...)`.
- **Dependency Injection**: Favor constructor injection over field injection (`@Inject`) to ensure testability and immutability.
- **Transactions**: Micronaut Data JDBC operations MUST be wrapped in `@Transactional` at the Service level (e.g., `ScanService.java`, `DashboardService.java`) whenever multiple queries or active connection context wrapping (`ContextualConnection`) is required.
- **Exceptions**: Use global Micronaut exception handlers mapping to appropriate HTTP status codes (e.g., `io.micronaut.http.exceptions.HttpStatusException`).

## Frontend (SolidJS & TailwindCSS)
- **Reactivity**: Strictly use SolidJS primitives (`createSignal`, `createResource`, `createEffect`, `createMemo`). Do NOT use React hooks (`useState`, `useEffect`).
- **Destructuring**: Do NOT intuitively destructure SolidJS props. Access them directly via `props.propertyName` to preserve Solid's proxy reactivity. 
- **Styling**: Leverage TailwindCSS utility classes directly. Maintain the established dark-mode aesthetics (e.g., `bg-zinc-900`, `text-emerald-400`, `border-zinc-800`).
- **Control Flow**: Exclusively use built-in SolidJS components `<Show>`, `<For>`, `<Switch>`, and `<Match>` rather than raw ternary JSX mapping.
