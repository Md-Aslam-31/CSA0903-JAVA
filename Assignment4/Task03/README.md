# Task 3 - Campus library search layout

`LibrarySearchFrame.java` implements the specified search field, category, search button, growing results panel, fixed navigation row and status message.

```text
JFrame (BorderLayout)
├── NORTH: JPanel (GridLayout 2x1)
│   ├── title JPanel (FlowLayout.LEFT)
│   └── search JPanel (FlowLayout.LEFT)
├── CENTER: JPanel (BorderLayout) -> JScrollPane -> JTextArea results
└── SOUTH: JPanel (BorderLayout)
    ├── navigation JPanel (FlowLayout.CENTER)
    └── status JLabel
```

BorderLayout reserves preferred-size north/south bands and allocates remaining width and height to CENTER. FlowLayout keeps the search row and buttons at their preferred component sizes. GridLayout makes the two fixed-height top rows uniform within the top panel; it doesn't govern the resizable center. A nested BorderLayout for root, center and bottom handles the growth cleanly.

## Resize evidence procedure

Launch `LibrarySearchFrame`, observe the initial 800 x 520 window, then resize the window and maximize it. At each size check the title/search controls remain in the NORTH area, the results viewport expands/contracts to consume the middle, and the navigation/status remain in SOUTH. The results panel uses a scroll pane so content remains accessible at the smaller size. Actual desktop captures of the default, compact/restored, and maximized window are saved in `outputs/library-default.png`, `outputs/library-compact.png`, and `outputs/library-large.png`.

## Alternative and complexity

One GridBagLayout could represent the same five regions with constraints and weights; it can preserve field sizes and give `weightx/weighty` to results, but the constraints are more verbose and must be maintained together. A single absolute-positioned container uses O(1) child lookup/layout bookkeeping for a fixed set of widgets, but each resize requires manual O(c) coordinate updates for c components and often clips on DPI/font changes. Nested panels add O(c) container/component references and normal layout traversals are O(c), where c is the number of child components. For this modest fixed screen the additional few containers are O(c) space and make resizing and maintenance predictable. GridBag's layout traversal is also O(c) for a fixed grid; constraint solving has larger constants but does not change the linear order for this form.
