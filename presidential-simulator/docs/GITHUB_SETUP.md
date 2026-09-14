# GitHub setup and feedback operations

This package supplies local files only. No remote repository or GitHub settings have been created.

## Initial setup

1. Choose the repository owner and final project name.
2. Complete the license decision in `LICENSING_AND_REVENUE.md` before advertising the project as open source.
3. Create the repository and push this directory's contents with `README.md` at the root.
4. Enable Issues and Discussions in repository settings.
5. Use Issues for actionable work; create Discussions categories for Questions, Ideas, Playtest stories, and Announcements.
6. Pin an introductory Discussion linking to the README and current milestone.
7. Create milestones M0–M6 from `ROADMAP.md`; add P01–P10 as the first issues.
8. Create a project board with Backlog, Ready, In progress, Review/playtest, and Done.
9. Add labels such as `bug`, `enhancement`, `playtest`, `documentation`, `accessibility`, `needs-reproduction`, and `good-first-issue`.
10. Once a build exists, add continuous integration for compile and meaningful engine tests. Protect the main branch with appropriate available review/check settings.

Issue templates become available from `.github/ISSUE_TEMPLATE/` when committed to the appropriate repository branch. The supplied templates use Markdown front matter and require no invented repository URLs. Enable Discussions separately; templates do not enable it. [GitHub templates](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/about-issue-and-pull-request-templates) · [Discussions quickstart](https://docs.github.com/en/discussions/quickstart)

## Triage

Review new reports weekly. Reproduce bugs, ask for missing details, link duplicates, and assign a milestone only when accepted. Feature requests should explain the player problem and smallest useful version. Close declined requests with a short rationale. Avoid promising dates for unstarted work.

Keep community discussion respectful and focused on the fictional game, rules, usability, and implementation. Feedback is not a referendum on real-world political preferences. Add a complete community conduct policy and a real moderation contact before inviting a large community.

## Player feedback button

When the GUI exists, add “Report a problem” and “Suggest a feature” links to this repository's actual issue chooser, plus a Discussions link. Let users review any diagnostic data before submission. Never include raw saves, private information, or browser data automatically.

GitHub participation generally requires an account. If that becomes a barrier, evaluate an optional feedback form later; do not add accounts to the game solely for feedback.

## Publishing the first release

Replace planning status only when the corresponding features ship. Create a version tag, attach the tested runnable artifact and any required runtime instructions, list known issues and save compatibility, and link to the matching source revision. Keep stable releases distinct from development snapshots.
