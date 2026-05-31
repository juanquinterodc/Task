# DreamCode Notes

DreamCode Notes is a simple, elegant, and secure Android application designed to help you capture every moment of inspiration. It is more than just a notepad; it is a personal digital sanctuary where your ideas flourish and your privacy is protected.

## 🚀 Key Features by Implementation Phases

### 🏁 Phase 0: Foundations & First Build
- **Project Genesis**: Initial setup targeting modern Android APIs (35/36) using Java and ViewBinding.
- **Core CRUD Functionality**: Complete system to Create, Read, Update, and Delete notes.
- **Local Persistence**: Integrated Room Database for secure, on-device storage.
- **Brand Identity**: Implementation of "DreamCode" visual identity, including custom colors, logos, and typography.
- **Release Ready**: Generation of the first Signed Android App Bundle (.aab) and preparation of mandatory Store declarations (Data Safety, Privacy Policy).

### 📁 Phase 1: Organization & Core
- **Smart Categories**: Efficiently organize your thoughts with professional tags: Work, Personal, Ideas, and General.
- **Advanced Search**: Instantly find any note with a high-performance search system integrated into the main dashboard.
- **Category Chips**: Quickly filter your entire library using a dedicated interactive chip group.
- **Robust Infrastructure**: Built on Room Database with automatic migrations and a scalable Note model.

### ⏰ Phase 2: Engagement & Sharing
- **Precise Reminders**: Never miss a deadline with high-precision date and time notifications using AlarmManager.
- **Advanced Sharing**: Export your notes as professionally branded images or clean plain text to any platform.
- **User Engagement**: Interactive UI elements designed to keep you focused on your content.

### 📝 Phase 3: Advanced Editor & Viewer
- **Note Visualizer**: A dedicated viewing screen with an action bar for quick editing, sharing, or deletion.
- **Interactive Checklists**: Create live to-do lists in the editor and toggle checkboxes directly in the viewer screen.
- **Rich Text Editor**: Express yourself with unified support for **Bold** and *Italic* formatting.
- **Live Checklist Support**: The editor automatically handles "☐ " prefixes on new lines for seamless list creation.
- **Professional Previews**: Automated formatting cleanup for the main list, ensuring a clean and readable look.

### 🔐 Phase 4: Security & Privacy
- **Secret Vault**: A protected section for sensitive notes, excluded from general lists and search results.
- **Biometric Protection**: Secure your vault with industry-standard Fingerprint or Face ID authentication.
- **PIN Fallback**: A custom 4-digit security code for devices without biometric hardware or as an alternative access method.
- **Security Settings**: Dedicated settings screen to manage your Vault PIN.
- **Secure Backups**: Configured Android data extraction rules to safely backup your notes and security settings to the cloud.

## 🛠 Technologies Used
- **Language**: Java
- **Database**: Room Persistence Library (SQL-based local storage).
- **Security**: Android Biometric Library & SharedPreferences Encryption.
- **Navigation**: Jetpack Navigation Component for seamless app flow.
- **UI Components**: Material Design 3, ViewBinding, and ConstraintLayout.
- **Background Tasks**: AlarmManager for high-precision notifications.

## Author
Developed with ❤️ by **Juan Quintero**.

---
*DreamCode Notes ensures performance and security on modern devices.*
