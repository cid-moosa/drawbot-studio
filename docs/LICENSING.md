# LICENSING & THIRD-PARTY NOTICES

## 1. Project License: GNU General Public License v3.0 (GPL-3.0)

Drawbot Studio is a derivative work based upon `Drawbot_image_to_gcode_v2` (v3.75 beta) created by **Scott Cooper (Dullbits.com, <scottslongemailaddress@gmail.com>)**.

In strict accordance with the terms of the **GNU General Public License version 3.0**, Drawbot Studio is licensed under GPL-3.0.

```
Drawbot Studio — Modernized Drawbot Image to G-Code Vector Generator
Copyright (C) 2017-2018 Scott Cooper (Dullbits.com)
Copyright (C) 2026 Antigravity Engineering Team

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

---

## 2. Derivative Work Modifications & Attributions

| Original Source / Component | Original Author | Modifications in Drawbot Studio |
|---|---|---|
| `Drawbot_image_to_gcode_v2.pde` | Scott Cooper | Re-architected from single-sketch Processing into modular Java desktop application with Swing GUI, interactive canvas, and multithreaded worker. |
| `Bresenham.pde` | Jack Elton Bresenham (1962), adapted by Scott Cooper | Implemented high-performance integer Bresenham ray rasterization and midpoint circle algorithm in `com.dullbits.drawbot.core.Bresenham`. |
| `Copic.pde` | Scott Cooper (Source: Imagination International) | Cleaned and preserved full catalog of 358 Copic marker codes, hex colors, and 18 marker set presets in `com.dullbits.drawbot.color.CopicPalette`. |
| `PFM_original.pde` | Scott Cooper | Preserved exact darkness-seeking squiggle algorithm, downsampled area search, and Bresenham ray testing in `com.dullbits.drawbot.pfm.PfmOriginal`. |
| `PFM_spiral.pde` | Based on SpiralFromImage by krummrey, adapted by Scott Cooper | Preserved Archimedean spiral with brightness amplitude modulation and direct pen mapping in `com.dullbits.drawbot.pfm.PfmSpiral`. |
| `PFM_squares.pde` | Scott Cooper | Preserved 4-way orthogonal sampling with sinusoidal angular modulation in `com.dullbits.drawbot.pfm.PfmSquares`. |
| `Gcode.pde` | Scott Cooper, with contributions from Vladimir Bochkov & John Cliff | Emits metric absolute G-code per pen, boundary test L-brackets, and Inkscape-compatible grouped SVG with polyline continuation in `com.dullbits.drawbot.export`. Fixed lowercase 'y0' syntax quirk. |
| `boarder/*.png` | Included in upstream repository (credits in `boarder/credits.txt`) | Retained unchanged in `assets/border/`. Corrected alpha transparency handling during additive blending. |

---

## 3. Third-Party Dependencies & Runtime Licenses

Drawbot Studio has been engineered to use **zero external third-party JARs** for its core functionality:
- All image filtering and convolution operations are implemented using standard Java SE `java.awt.image`.
- All G-code, SVG, and vector PDF generators are implemented in pure Java without third-party PDF or CAD libraries.
- The bundled runtime image packaged via `jpackage` contains OpenJDK Temurin components licensed under the **GNU General Public License v2 with Classpath Exception (GPLv2+CE)**.

---

## 4. Source Code Distribution

In compliance with Section 6 of the GPL-3.0, the complete corresponding source code for Drawbot Studio is included alongside all binary distributions in the `src/` directory, allowing any recipient to rebuild, modify, and redistribute the application under the same terms.
