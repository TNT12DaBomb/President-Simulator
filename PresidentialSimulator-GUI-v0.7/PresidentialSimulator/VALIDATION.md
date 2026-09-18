# v0.7 validation

Passed `sh test-debates.sh`: Java compilation with all lint checks; 140 live cards with unique prompts, valid choices and nonempty sources; six tutorial cards disjoint from live prompts; fixed practice transcripts across seeds; 200 seeds per mode with deterministic selection and three distinct topics per appearance; both tutorial protocols complete; tutorial choices create no campaign commitment; headless Swing checks readable controls, timer-only updates preserving button instances, captured response revisions and one Continue control.

Native operating-system modal behavior and visual device testing remain unverified in this headless environment. Existing broad regression suites were not run and contain older inline-interface assumptions. Election model behavior was not modified or tested in this release.
