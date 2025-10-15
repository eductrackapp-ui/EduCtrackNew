# Parent Portal - Add Child Feature

## Overview
Implemented a complete "Add Child" functionality for the parent portal that allows parents to register their children with the school system and automatically generate unique student codes for tracking exam results and grades.

## Features Implemented

### 1. Add Child Activity (`AddChildActivity.java`)
- **Location**: `app/src/main/java/com/equipe7/eductrack/Activity/AddChildActivity.java`
- **Purpose**: Allows parents to register a new child in the system

#### Key Features:
- **Child Information Fields**:
  - Child's Full Name
  - School Branch Selection (Kacyiru, Gisozi, Kimisagara)
  - Class Name (e.g., 5A, 6B)
  - Grade Level (e.g., Grade 5)

- **Auto-Generated Student Code**:
  - 8-character alphanumeric code (e.g., `ABC12XYZ`)
  - Automatically generated on screen load
  - Can be refreshed using the "Refresh" button
  - Uniqueness validation against Firestore database
  - Read-only field to prevent manual editing

- **Data Persistence**:
  - Saves to Firestore `children` collection
  - Associates child with current parent's UID
  - Stores: name, className, grade, studentCode, branch, parentId, schoolYear, status

### 2. Enhanced Child Model (`Child.java`)
- **Location**: `app/src/main/java/com/equipe7/eductrack/models/Child.java`
- Added new fields:
  - `studentCode` - Unique identifier for the child
  - `branch` - School branch (Kacyiru, Gisozi, Kimisagara)
- Includes getters and setters for new fields

### 3. Parent Home Screen Integration
- **Updated**: `ParentHomeActivity.java`
- **Updated**: `parent_home_activity.xml`
- Added prominent "Add New Child" button in the Quick Actions section
- Click handler opens `AddChildActivity`

### 4. Auto-Loading Student Results
- **Enhanced**: `StudentExamResultsActivity.java`
- Automatically loads student code from parent's first child
- Pre-fills student code field when opening exam results
- Auto-loads exam results if student code is available
- Manual entry still supported as fallback

### 5. UI Components Created
- **Layout**: `activity_add_child.xml`
  - Modern Material Design with gradient background
  - Dropdown for school branch selection
  - Auto-complete text view for branch selection
  - Progress indicators for loading states
  - Informative message about student code usage
  - Professional card-based layout

- **Drawable Icons**:
  - `ic_school.xml` - School building icon
  - `ic_class.xml` - Classroom/chart icon
  - `ic_grade.xml` - Star/grade icon

### 6. Manifest Registration
- Added `AddChildActivity` to AndroidManifest.xml
- Set as non-exported activity (internal use only)

## How It Works

### Adding a Child:
1. Parent opens Parent Home screen
2. Clicks "Add New Child" button
3. Fills in child information:
   - Name
   - Selects school branch from dropdown
   - Enters class and grade
4. System auto-generates unique 8-character student code
5. Parent can refresh code if needed
6. Clicks "Add Child" button
7. Child is saved to Firestore with all details
8. Success message shows the student code
9. Returns to parent home screen

### Using Student Code:
- Student code is displayed in success message
- Parents should save this code
- Used to load exam results in "Exams" section
- Code is auto-filled when parent opens exam results
- Can be manually entered if needed

## Database Structure

### Firestore Collection: `children`
```javascript
{
  "name": "John Smith",
  "className": "5A",
  "grade": "Grade 5",
  "studentCode": "ABC12XYZ",
  "branch": "Eden Family School Kacyiru",
  "parentId": "firebase_auth_uid",
  "overallAverage": 0.0,
  "status": "good",
  "schoolYear": "2024-2025",
  "createdAt": 1234567890
}
```

## School Branches
The system supports three school branches:
1. **Eden Family School Kacyiru**
2. **Eden Family School Gisozi**
3. **Eden Family School Kimisagara**

## Student Code Format
- **Length**: 8 characters
- **Format**: Alphanumeric (A-Z, 0-9)
- **Example**: `KY7X4R9M`
- **Uniqueness**: Validated against existing codes in database
- **Generation**: Random with collision detection

## Future Enhancements (Recommendations)
1. **Multiple Children Support**: Display list of all children on parent home
2. **Edit Child**: Allow editing child information
3. **Delete Child**: Remove child from system
4. **Child Switching**: Switch between multiple children's data
5. **QR Code**: Generate QR code for student code
6. **Code Export**: Email or download student code
7. **Branch-Specific Data**: Filter data by school branch
8. **Performance Analytics**: Branch-based performance comparison

## Testing Checklist
- [x] Add child with all fields filled
- [x] Verify student code generation
- [x] Check uniqueness validation
- [x] Test branch selection dropdown
- [x] Verify data saved to Firestore
- [x] Test navigation from parent home
- [x] Verify exam results auto-load with student code
- [ ] Test with multiple children per parent
- [ ] Test code refresh functionality
- [ ] Verify error handling for network issues

## Files Modified/Created

### Created:
1. `AddChildActivity.java`
2. `activity_add_child.xml`
3. `ic_school.xml`
4. `ic_class.xml`
5. `ic_grade.xml`
6. `PARENT_ADD_CHILD_FEATURE.md` (this file)

### Modified:
1. `Child.java` - Added studentCode and branch fields
2. `ParentHomeActivity.java` - Added Add Child button handler
3. `parent_home_activity.xml` - Added Add Child button UI
4. `StudentExamResultsActivity.java` - Added auto-load for student code
5. `AndroidManifest.xml` - Registered AddChildActivity

## Notes
- Student codes are permanent and cannot be changed
- Each child must have a unique student code
- Parent ID is automatically set from logged-in user
- Branch selection is required (cannot be empty)
- All validations are performed before saving
- Network connectivity required for all operations
