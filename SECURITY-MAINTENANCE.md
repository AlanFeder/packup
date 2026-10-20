# Prototype dependency maintenance

The Next.js prototype now uses patched Next.js 16 / React 19 and refreshed transitive packages. Use Node 24.12+ and pnpm 11.25.0 in `vercel_prototype`; run `pnpm install --frozen-lockfile`, `pnpm build`, and `pnpm audit`.

CI validates the prototype's complete locked dependency graph and production build. The native Android runtime was resolved separately: 219 Maven components were checked with OSV. A Protobuf denial-of-service advisory (CVE-2024-7254) affected the Firebase-selected protobuf-javalite 3.25.1. A Gradle constraint now selects compatible 3.25.9; the updated runtime scan reports no known advisories. The native CI job resolves and audits this graph using a checksummed OSV Scanner release. This covers runtime dependencies, not every Android SDK/build-plugin component.
