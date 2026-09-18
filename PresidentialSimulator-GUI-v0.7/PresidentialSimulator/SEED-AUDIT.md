# Opening map and debate continuity — unresolved

Source inspection: `SyntheticElectorate.java` initializes geography with `new Random(0x715a2L ^ state.getName().hashCode())`. The selected seed is absent from this opening geography calculation. Seed-dependent election-day variation is a separate path. Consequently changing only the seed does not change the opening geography.

`LiveDebate.java` records sourced answers, confidence, corrections and public statements. It does not supply a reputation modifier to voting calculations. Policy commitments remain procedural history and can be compared with subsequent decisions.

Neither calculation was changed in v0.7. The seed does control live question selection and answer ordering; fixed practice deliberately ignores it. Question-bank randomness must not be mistaken for an opening-map fix.
