# Task 2 - Outpatient appointment desk

## Event table

| Source | Event object | Listener | Action |
|---|---|---|---|
| Department JComboBox | ItemEvent / ActionEvent | ItemListener or ActionListener | Replace doctor choices from the in-memory department map. |
| Book JButton | ActionEvent | ActionListener | Validate fields, mark booking in progress, submit through a worker, show confirmation/error. |
| Patient ID JTextField | ActionEvent | ActionListener (optional) | Invoke the same booking command when Enter is pressed. |
| Slot JComboBox | ItemEvent | ItemListener (optional) | Update current slot choice. |
| JFrame close control | WindowEvent | WindowAdapter.windowClosing | If booking is active, ask before disposing; otherwise close. |

## Component containment tree

```text
JFrame AppointmentDesk (BorderLayout)
├── CENTER: JPanel (GridLayout 4x2)
│   ├── JLabel + JTextField patientId
│   ├── JLabel + JComboBox department
│   ├── JLabel + JComboBox doctor
│   └── JLabel + JComboBox timeSlot
└── SOUTH: JPanel (FlowLayout.RIGHT)
    └── JButton book
```

## Handler pseudocode

```text
PROCEDURE refreshDoctors
    department <- SELECTED_VALUE(departmentChoice)
    doctorChoice.removeAllItems()
    FOR EACH doctor IN doctorMap.get(department)
        doctorChoice.addItem(doctor.displayName)
    END FOR
END PROCEDURE

PROCEDURE bookAppointment
    patient <- TRIM(patientIdField.text)
    IF patient is not a valid ID OR doctorChoice has no selection THEN
        SHOW validation error
        RETURN
    END IF
    bookingInProgress <- TRUE
    START background booking operation(patient, department, doctor, slot)
    WHEN operation finishes on event thread:
        bookingInProgress <- FALSE
        SHOW confirmation or actionable error
    END
END PROCEDURE
```

## Event trace and applet comparison

`main -> SwingUtilities.invokeLater -> AppointmentDesk constructor -> build controls -> attach listeners -> refreshDoctors -> pack -> setVisible -> event queue dispatches user actions -> windowClosing -> optional close confirmation -> dispose`. Frame activation/painting and subsequent window events are managed by the windowing toolkit; an applet instead receives container lifecycle callbacks `init`, `start`, `paint`, `stop`, and `destroy`. A standalone Swing frame has no applet lifecycle contract.

## Alternative: query the database on every department change

For each change, a database-backed approach pays connection/network/SQL latency `L` plus result processing O(d_j), where d_j is the number of doctors returned for the selected department. Loading all doctors once costs O(m) initial work and O(m) memory for m doctors across d departments; each change then takes O(d_j) to retrieve a list reference (plus O(d_j) to populate the combo box). Query-on-change uses O(d_j) result memory at a time but repeats round trips and may show stale/failed states. A local `Map<String,List<Doctor>>` uses O(d+m) entries/pointers, but avoids that repeated latency. In a production app, refresh the map by TTL or explicit invalidation if staffing changes.
