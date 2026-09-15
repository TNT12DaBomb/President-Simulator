# Licensing, hosting, and revenue plan

Planning guidance checked September 14, 2026. No license, advertising account, deployment, or revenue collection has been activated by this package.

## Source license

Recommended starting choice: **MIT**, provided commercial and closed-source forks are acceptable. It is a straightforward fit for broad reuse and a small community project. Select **GPLv3** instead if source sharing for distributed derivative works is an important project goal.

| Choice | Commercial use | Important distinction |
| --- | --- | --- |
| MIT | Permitted | Recipients preserve copyright/license notices; derivatives may use different terms and remain closed-source. |
| GPLv3 | Permitted | Distribution carries copyleft and corresponding-source obligations for covered works. |

Sources: [MIT license summary and text](https://choosealicense.com/licenses/mit/) · [GPLv3 summary and text](https://choosealicense.com/licenses/gpl-3.0/)

Neither choice reserves commercial use or advertising to the original developer. If preventing others from making commercial forks is essential, that conflicts with the ordinary open-source goal; do not describe a noncommercial restriction as an open-source license. [Open Source Definition](https://opensource.org/osd)

GPLv3 is primarily triggered by conveying software, not merely providing network access to a server. If a later server implementation makes network copyleft a priority, separately evaluate AGPL rather than assuming GPLv3 covers that case. [GPLv3 section 0](https://choosealicense.com/licenses/gpl-3.0/)

## Applying the chosen license

1. Establish authorship and permission for all supplied code. A project made for a friend does not by itself establish whether the friend is an author or owner.
2. Pick MIT or another specific license, including the precise GPL version policy if selected.
3. Add the complete official license text as root `LICENSE`, with the correct notices and copyright holder information as applicable.
4. Update the README from “proposed” to the selected license and link to `LICENSE`.
5. Record third-party code and dependency notices before distributing them.
6. Specify inbound contribution terms so contributors know the project license before their work is accepted.

The package intentionally does not insert a speculative copyright holder or apply a license to potentially shared work. This is a publication decision, not a blocker to local prototype development.

## Assets and branding

Maintain an asset register: file, creator, original source, license, attribution, modifications, and evidence of permission. Do this for maps, fonts, audio, photographs, icons, and writing. An open-source code license does not establish rights to third-party assets. Choose a separate explicit policy for original assets and branding before release.

Use original visual design and content for the references to Oregon Trail and Plague Inc. Those titles express inspiration, not affiliation or permission to reuse assets.

## Revenue sequence

1. **Text alpha:** free downloads, no advertising integration. Test whether people enjoy complete runs.
2. **Community stage:** evaluate voluntary sponsorship as a simpler experiment, with accurate expectations about what supporters receive.
3. **Hosted GUI:** consider an ad-supported official edition when it has a stable experience and recurring users.
4. **Review:** compare actual net receipts with hosting, support, and development costs. Continue only if the tradeoff is worthwhile.

Advertising belongs to the delivery/UI layer. The engine should not know about ad impressions, accounts, or payment providers. Ad blockers and provider outages must not prevent play or affect game outcomes.

Start with limited placements around the landing page or post-run summary. Avoid positioning ads beside decision buttons or designing interactions that encourage accidental clicks. Google's placement rules specifically address accidental clicks and placements near interactive applications. [AdSense placement policies](https://support.google.com/adsense/answer/1346295?hl=en)

AdSense is an example to evaluate, not a committed provider. Eligibility requires suitable original content and policy compliance; applying does not guarantee approval. Review the final game's content and site experience against current requirements. [AdSense eligibility](https://support.google.com/adsense/answer/9724?hl=en)

Before enabling a provider, document what it collects and implement any consent/privacy behavior required for the chosen audience and regions. Review current provider requirements at that time; this package is not a jurisdiction-specific compliance assessment.

## Financial model

Use measured inputs rather than income promises:

`monthly gross ad revenue = monetized impressions / 1,000 × effective revenue per 1,000 impressions`

Then subtract hosting, storage, domain, services, and applicable fees. Effective rates and fill vary, so early revenue may be negligible. Distinguish sessions, page views, ad requests, filled impressions, and actual receipts; they are not interchangeable.

Track: completed runs, voluntary replay feedback, recurring visits if measured appropriately, server cost per session, ad failure behavior, and actual monthly expenses. Begin with aggregate operational metrics and avoid requiring player accounts merely to measure usage.

## Hosting

GitHub can host the source and feedback workflow. It need not host the revenue-generating game. GitHub Pages has restrictions on use as an online business or commercial SaaS; do not assume it is the production home for a commercial hosted edition. [GitHub Pages limits](https://docs.github.com/en/pages/getting-started-with-github-pages/github-pages-limits)

Choose commercial hosting after the GUI delivery decision. Evaluate static hosting if gameplay runs locally in the browser; evaluate application hosting if retaining a Java server. Budget for bandwidth, compute, session storage, monitoring, and recovery. Do not introduce a database until there is a concrete persistence requirement beyond local saves.
