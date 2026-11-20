# Task 4 - Event-handling forensics

## Defect and failing interactions

`mouseclicked` is incorrectly cased; the listener method is `mouseClicked(MouseEvent)`. Without `@Override`, Java sees a new unrelated method and the adapter's inherited no-op handles the actual click, so count remains zero. Even with the spelling repaired, `getGraphics()` draws only into the current screen surface. Click once, minimize and restore (or cover/uncover) the window: the OS requests repaint and the temporary pixels are lost because no persistent model is repainted. The label also isn't updated in the original code.

`DrawPad.java` stores each point, updates the label, calls `repaint()`, and draws every point in `paint`. Coordinates are adjusted by half the 6-pixel diameter so the circle is centered on the click. `DrawPadBroken.java` preserves the deliberate typo with `@Override`. Compile it to capture `method does not override or implement a method from a supertype`; the compiler catches the mismatch before runtime.

## Reproduction

Run `DrawPad`, click three different locations, record `Clicks: 3`, minimize and restore: the three dots and count should remain. For the original defect, substitute `mouseclicked` with no annotation; clicks do not change state. For repaint loss, use the original `getGraphics()` one-shot drawing after correcting the method name, then minimize/restore.

## Alternative and complexity

For n dots, iterating stored `Point` objects costs O(n) work per repaint and O(n) point storage. A `BufferedImage` can be drawn incrementally when clicks arrive, then copied during `paint`; after construction each repaint copies O(w*h) pixel data (or O(1) draw call with device-dependent raster transfer), and the image costs O(w*h) pixels, where w and h are drawable dimensions. The image avoids replaying all n draw operations, but needs more memory and must be rebuilt on resize/DPI changes. For sparse dots or small n, the point list is simpler and memory-efficient.

## Evidence status

The source, exact javac diagnostic, and real running-app screenshots are in `outputs/`. The before-minimize and after-restore captures show the three persistent points and `Clicks: 3`.
