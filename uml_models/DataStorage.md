
# Patient Data Management Architecture

### Data Storage & Structure
- `VitalsData` - Simple data structure for vital signs. String-based value representation provides flexibility in handling different measurement units and formats.
- `PatientData` stores vitals readings with timestamps.
- `DataStorage` uses a nested map (PatientID → TreeMap<timestamp, PatientData>) which makes it quick to look up historical data for a patient. Meaning, all of the patient's data is associated with a patientID. The TreeMap allows to access data based on timestamps quickly.

### Data Operations
- `DataSaver` gives a simple way to store patient data while handling access control behind the scenes. It keeps things clean by hiding how the data is actually stored.
- `DataRemover` - delete records either by patient ID or a time range.
- `OldDataCleaner` runs cleanup tasks to delete old data, based on soft and hard time limits. The hard limit is for data that will be automatically deleted (so for example 5 years or etc). The soft limit is for the "cleanup" method, so when this method will be called, all data older than the soft limit will be deleted. 
- `DataRetriever` - use a query string to retrieve a list of PatientData. 

### Access Control
- `AccessLevel` is an enum with roles like Admin, Doctor, Nurse, and Patient. It controls what kind of data each role is allowed to see.
- Every data-related action needs an AccessLevel argument, so access checks are always enforced.

This architecture allows:
1. Store and find patient data over time efficiently and securely
2. Control who sees what, depending on their role (access level)
3. Clean up old or unnecessary data automatically
4. Keep storage logic separate from how data is used
5. Support data queries, which helps with reporting and analysis
