# v0.7.1 validation

Passed: seeded opening reproducibility across 40 seeds with more than ten distinct EV totals; good versus poor debate performance produces different credibility and map projections; higher credibility improves actual election-day vote share in every tested state; save/load reproduces the campaign snapshot after a real debate and retains headlines. Previous 140-card bank, fixed practice, topic rotation and stable debate panel checks still pass.

Headless GUI test confirms dollar formatting, headline presence, stable headline position across refresh, and 1024x650 rendering. Screenshot inspected: readable funds, headline beneath stats, no overlap. Build compiles with lint checks and bundled JAR rebuilt.

Native modal focus remains unverified. Older broad test scripts contain assumptions about earlier UI labels and are not the v0.7.1 validation gate. Use test-consequences.sh. Long-term balance coefficients remain authored game assumptions, not empirical forecasts.
