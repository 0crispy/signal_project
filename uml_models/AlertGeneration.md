# AlertGeneration

### VitalsThreshold & PatientThresholdProfile
- `VitalsThreshold` is an interface to enable multiple threshold implementations (blood pressure, heart rate, oxygen levels, etc). Meaning, you can specify complex behaviour, like alert if value A is below X, but not if it's below Y.
- `PatientThresholdProfile` holds the thresholds. Each patient can have a separate profile, but profiles may also be re-used for the same patient.

### VitalsData
- Simple data structure for vital signs
- String-based value representation provides flexibility in handling different measurement units and formats.

### Alert System
- `Alert` objects store all necessary information (patient ID, vital data, timestamp).
- `AlertGenerator` receives callbacks from `PatientDataReader`s, which contain the vitals data. It then uses that data to check the thresholds and generate an `Alert`, if the some value exceeds the thresholds.
- `AlertManager` receives Alerts from the `AlertGenerator` and sends them to the medical staff.

### Data Access
- `PatientDataReader` interface can read from any source - files, TCP, WebSockets, etc.
- `PatientDataGenerator` interface generates simulation data. 

### Staff Management
- `MedicalStaff` entities maintain subscriptions to specific patients, enabling targeted alerting.
- The subscription system ensures that the staff only receive relevant notifications.

This architecture allows:
1. To read the vital information from any source and easily add new read implementations
2. To generate alerts based on patient profiles
3. To generate alerts based on complex thresholds and easily add new thresholds
4. To alert only the medical staff who have subscribed to the patient.