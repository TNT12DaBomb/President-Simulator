# Contributing

Thanks for helping improve Presidential Simulator. The current package is an early prototype and development plan. Source licensing must be finalized before accepting public contributions under project-wide terms.

## Useful contributions

- Reproducible bug reports and playtest observations.
- Small engine changes tied to an accepted issue.
- Original event writing with clear prerequisites and consequences.
- Documentation and accessibility improvements.
- Art or audio with documented authorship and compatible rights.

## Workflow

1. Read the README, known issues, and roadmap. Search existing reports.
2. For a substantial feature, discuss the player problem and scope before implementation.
3. Create a branch and keep the change focused on one issue.
4. Build using README instructions; after test tooling exists, run relevant tests.
5. For rule changes, provide deterministic examples and document save compatibility.
6. Update documentation when behavior changes and submit a pull request using the template.

Keep simulation rules outside UI classes. Avoid introducing dependencies without explaining why they are needed. Never commit secrets, personal information, generated build outputs, or third-party assets without rights information.

Bug reports should include version/commit, platform, steps, expected and actual behavior, and seed/configuration where available. The current prototype has no seed input or save files.

For narrative contributions, use original fictional material, make choices understandable, and document intended consequences. Keep factual reference material sourced separately from scenario invention.

Once a license is selected, maintainers must update this file with the actual inbound contribution terms before accepting contributions. No contributor agreement is implied by this planning document.
