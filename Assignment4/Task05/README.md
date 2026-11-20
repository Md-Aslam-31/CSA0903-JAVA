# Task 5 - Four event-wiring styles

`ConverterStyles` accepts `self`, `named`, `anonymous` or `lambda`. Each style handles button click and Enter on the amount field, and a `WindowAdapter` handles closing. `Converter.java` fills the supplied lambda skeleton, shares one lambda listener across the button and field, displays two decimal places and reports invalid input. Conversion rates are fixed teaching examples (INR 83 per USD, INR 90 per EUR in `ConverterStyles`; the supplied-code exercise uses the provided 0.012 and 0.011 factors).

| Style | Handler | Access to frame fields | Reuse/change when Enter + dialog are added |
|---|---|---|---|
| Frame implements ActionListener | `this` | Direct | Add second registration and update one method; concise but mixes frame and command role. |
| Named inner class | `NamedHandler` | Direct | Reuse one handler for both triggers; moderate extra declaration, easiest to extend with state. |
| Anonymous inner class | One inline object | Direct | Attach the same local object to second source; code remains local but less reusable elsewhere. |
| Lambda | `e -> doConvert()` | Lexical access | One lambda variable can be registered twice; shortest for small stateless actions. |

Change-effort record: for each version the same handler is registered on two sources (button and text-field action), and the shared conversion method shows the dialog. The implementation-specific change is one extra registration line for Enter, zero new conversion algorithms; adding a dialog is one shared method edit. `Task05/outputs/change-effort.csv` counts concrete added registrations/changed methods in this solution. For a large application, a named command class or Swing `Action` is preferable when the command has state, is reused by menus/toolbars/shortcuts, needs enabled state, or merits unit tests. Lambda is best for a short, local action.

## Alternative: method reference or shared Action

`button.addActionListener(this::doConvert)` uses a method reference and creates an adapter object; a reusable `AbstractAction` can be installed on a button and menu item and bound to a keyboard shortcut with an input/action map. One action centralizes labels, enabled state and invocation. Four independently written listeners use O(k) listener objects/storage for k triggers; one shared Action uses O(1) action object plus O(k) toolkit registration references. Adding a new trigger is O(1) source change (one binding) when the Action exists; command behavior is still O(1) per event, excluding conversion math.
