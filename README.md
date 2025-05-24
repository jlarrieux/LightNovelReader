# Light Novel Reader

A powerful Android application for reading light novels from various web sources with text-to-speech support and advanced novel management features.

## 📱 Overview

Light Novel Reader is an Android app that extracts clean text from light novel websites, removing HTML tags and unwanted content, then provides multiple reading options including text-to-speech integration. The app supports managing your novel library with features like categorization, progress tracking, and custom filtering.

## ✨ Features

### Core Reading Features
- **Web Parser Integration**: Extract clean text from multiple light novel websites
- **Text-to-Speech Support**: Built-in TTS engine for hands-free reading
- **Intent Sharing**: Send cleaned text to other reading apps like [@Voice Aloud Reader](https://play.google.com/store/apps/details?id=com.hyperionics.avar&hl=en_US&gl=US)
- **Chapter Navigation**: Easy navigation between previous/next chapters
- **Offline Reading**: Save novels locally for offline access

### Novel Management
- **Library Organization**: Categorize novels by reading status
- **Long-Press Actions**: Quick access menu for novel management
  - Mark as Completed
  - Move to Dropped
  - Plan to Read
  - View Details
  - Delete Novel
- **Progress Tracking**: Keep track of your current chapter and last read date
- **Search & Filter**: Find novels quickly in your library

### Customization
- **Blocked Strings**: Filter out unwanted text patterns (ads, repeated phrases)
- **Website Synonyms**: Handle different domain variations for the same novel site
- **Parser Management**: Enable/disable specific website parsers

## 🌐 Supported Websites

The app includes parsers for popular light novel websites:

- **FreeWebNovel** (freewebnovel.com and synonyms)
- **LightNovelReader** (lightnovelreader.org)
- **MTLReader** (mtlreader.com)
- **NovelTop** (noveltop.net)
- **RoyalRoad** (royalroad.com)
- **InfiniteNovelTranslation** (infinitenoveltranslations.net)
- **Custom Parsers**: Extensible architecture for adding new sites

## 🛠️ Technical Details

### Requirements
- Android 5.0 (API 21) or higher
- Internet connection for fetching novels
- Storage permission for saving novels locally

### Architecture
- **Language**: Java
- **Min SDK**: 21
- **Target SDK**: 33
- **Build System**: Gradle

### Key Components
- `MainActivity`: Main UI and navigation
- `WebParser`: Base class for website parsers
- `NovelDialogFragment`: Novel selection and management
- `SaverLoaderUtils`: Local storage management
- `TtsUtteranceListener`: Text-to-speech event handling

## 📲 Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/LightNovelReader.git
```

2. Open the project in Android Studio

3. Build and run on your device or emulator

## 🚀 Usage

### Reading a Novel
1. Enter or paste a novel chapter URL
2. Tap the play button to start reading
3. Use navigation buttons for previous/next chapters

### Managing Your Library
1. Access your saved novels from the menu
2. Long-press any novel for quick actions
3. Organize novels by reading status

### Customizing Filters
1. Open Settings from the overflow menu
2. Add blocked strings to filter unwanted content
3. Manage website synonyms for consistent parsing

## 🔧 Development

### Adding a New Parser
1. Extend the `WebParser` base class
2. Implement required methods:
   - `getNovelName()`
   - `getNextLink()`
   - `getPreviousLink()`
   - `getCleanedHTML()`
3. Register the parser in `URLHandler`

### Project Structure
```
app/
├── src/main/java/com/jeannius/lightnovelreader/
│   ├── MainActivity.java
│   ├── webparser/
│   │   ├── WebParser.java
│   │   └── [Site-specific parsers]
│   ├── DialogFragment/
│   │   └── [UI dialogs]
│   └── Interface/
│       └── [Listener interfaces]
└── src/main/res/
    ├── layout/
    ├── menu/
    └── values/
```

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Thanks to all contributors and users
- Special thanks to the light novel community
- [@Voice Aloud Reader](https://play.google.com/store/apps/details?id=com.hyperionics.avar) for TTS integration inspiration

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Contact the maintainers

---

**Note**: This app is for personal use only. Please respect the copyright and terms of service of the websites you access.