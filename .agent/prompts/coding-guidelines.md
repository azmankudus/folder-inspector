---
description: Code Style and Agentic Generation Strict Constraints
---
# Coding Implementation Constraints

When an agent (Gemini, Claude, or Codex) is modifying or synthesizing code for this repository, it must unconditionally conform to the following baseline structures:

1. **Reactive SolidJS Ecosystem**: 
   - Never implement React-specific artifacts (`useState`, `useEffect`). Utilize `createSignal` and `createResource`.
   - Use built-in `<Show>`, `<For>`, `<Switch>` templates instead of standard JSX mapping parameters.
   - Do NOT destructure SolidJS Component `props` dynamically (this severs reactivity proxies).

2. **Java 21 & Micronaut Data**: 
   - Favor DTO definitions via immutable Java 21 `record` constructs instead of POJOs whenever mathematically possible.
   - You MUST enforce Micronaut Database pool contexts by strictly annotating aggregate domain services with `jakarta.transaction.Transactional` so proxy delegates maintain the underlying database pool. 

3. **Security Protocol**:
   - Limit Role Mapping strictly to stateless `API_XXX_YYY` strings via Micronaut's `@Secured` annotations. Rely exclusively upon standard JWT transmission models.
