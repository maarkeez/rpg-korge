# Subdomain dependencies are acyclic and can only shrink

No subdomain may depend on another in a cycle, even indirectly, and each restricted subdomain (e.g.: `shared`, `unit`,
`battlefield`, `battleUnit`) may only depend on the subdomains `SubdomainDependencyArchitectureTest` lists for it. Each
list holds exactly what the subdomain depends on today, so it can only shrink: a new dependency fails the test, and one
no longer used must be removed from the list. We chose a ratchet over a target architecture drawn up front so the
dependencies never grow while they are untangled one at a time (e.g.: `battlefield` → `battle` is the next to go). The
[dependency report](../architecture/dependencies.html), regenerated on every commit, highlights the same cycles the test
fails on.
