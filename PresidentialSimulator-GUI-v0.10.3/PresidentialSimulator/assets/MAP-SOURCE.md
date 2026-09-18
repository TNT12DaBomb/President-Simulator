# Offline map geometry

Source: https://cdn.jsdelivr.net/npm/us-atlas@3/states-albers-10m.json
Documentation: https://github.com/topojson/us-atlas

US Atlas distributes simplified 2017 U.S. Census cartographic boundaries, projected using Albers USA into a 975×610 viewport. Alaska and Hawaii are insets. The included US-ATLAS-LICENSE.txt applies to the redistributed geometry.

states.bin contains the decoded paths: big-endian int geometry count, then UTF-8 state name with unsigned-short byte length, int ring count, and for each ring an int point count followed by pairs of float32 x/y coordinates. Reversed TopoJSON arcs are joined in reverse order. Rings use even-odd filling. No new boundaries are inferred.

The renderer scales these paths uniformly and adds enlarged small-state callouts. Data is bundled inside the JAR and requires no network connection during play. Geometry is separate from the existing simulation's state data.
