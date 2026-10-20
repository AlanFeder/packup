# Prototype dependency maintenance

The Next.js prototype now uses patched Next.js 16 / React 19 and refreshed transitive packages. Use Node 24.12+ and pnpm 11.25.0 in `vercel_prototype`; run `pnpm install --frozen-lockfile`, `pnpm build`, and `pnpm audit`.

CI validates the prototype's complete locked dependency graph and production build. This workflow does not build or claim to audit the native Android application, whose Gradle version catalog is a separate dependency graph.
