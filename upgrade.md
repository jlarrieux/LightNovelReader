# Light Novel Reader App Redesign Plan

## Overview
This document outlines the planned navigation and UI redesign for the Light Novel Reader app, focusing on improving user experience by replacing popup menus with proper navigation and adding novel categorization features.

## Current Issues
- Multiple popup menus for navigation (poor UX)
- No way to organize novels by reading status
- Cluttered novel list with all novels mixed together
- No visibility of latest chapters in novel lists

## Proposed Navigation Architecture

### Bottom Navigation Structure
Replace current overflow menu with bottom navigation containing 4 main sections:

1. **Reader** - Current reading interface
2. **Library** - Novel management with categories
3. **Parsers** - Website parser management
4. **Settings** - App settings, blocked strings, synonyms

### Library Section Design

#### Novel Categories
Each novel can be assigned one of these statuses:
- **Currently Reading** (default)
- **Completed**
- **Dropped/Not Interested**
- **Plan to Read**

#### Library Tabs/Sections
- **Currently Reading** - Active novels only
- **Completed** - Finished novels
- **Dropped** - Novels user is not interested in
- **All Novels** - Complete list with filters

#### Novel List Item Information
Each novel entry should display:
- Novel title
- Current chapter  (e.g., "Ch. 245")
- Last read date
- Reading progress indicator
- Status badge/tag

## Feature Improvements

### Novel Management
1. **Long-Press Actions**
   - Long press on novel: Show popup menu with options
   - Popup menu actions:
     - Mark as Completed
     - Move to Dropped
     - Move to Plan to Read
     - Delete Novel
     - View Details

2. **Smart Features**
   - Auto-move to "Currently Reading" when accessed
   - Track last read timestamp
   - Quick status change from any list

3. **Latest Chapter Tracking**
   - Fetch and display latest available chapter
   - Show update indicators for new chapters

### UI/UX Enhancements
1. **Filter**
   - Filter by status, parser, update date
   - Sort options (alphabetical, last read, recently updated)

2. **Material Design 3**
   - Modern UI components
   - Dynamic color theming
   - Smooth transitions between sections

3. **Data Management**
   - Backup novel status and progress
   - Reading statistics dashboard

## Implementation Phases

### Phase 1: Navigation Restructure
- Implement bottom navigation
- Create fragment structure for each section
- Move existing functionality to new navigation

### Phase 2: Novel Categorization
- Add status field to novel data model
- Implement category tabs in library
- Add swipe actions and status management

### Phase 3: Chapter Tracking
- Implement latest chapter fetching
- Add chapter info to novel list items
- Create update checking service

### Phase 4: Polish & Additional Features
- Material Design 3 update
- Search and filter implementation
- Statistics and export features

## Technical Considerations

### Data Model Changes
- Add `status` field to Novel model (enum: READING, COMPLETED, DROPPED, PLAN_TO_READ)
- Add `lastReadDate` timestamp field
- Add `latestChapter` and `currentChapter` fields
- Add `totalChapters` field (if available from parser)

### Database Migration
- Create migration to add new fields
- Set default status to READING for existing novels
- Preserve existing novel data

### UI Components
- Use `BottomNavigationView` for main navigation
- Use `ViewPager2` with `TabLayout` for library sections
- Implement `RecyclerView` with long-press popup menu
- Use `MaterialCardView` for novel list items
- Use `PopupMenu` for novel actions on long-press

## Future Enhancements
- Cloud sync for reading progress
- Social features (share reading lists)
- Reading goals and achievements
- Chapter download for offline reading
- Custom categories/tags
- Reading time tracking