# Light Novel Reader - Long Press Popup Menu Mockup

## UI Layout Description

### Main Novel List View
```
┌─────────────────────────────────────────┐
│ 📚 Light Novel Reader                   │
│─────────────────────────────────────────│
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🔷 Overlord                         │ │
│ │ Chapter 156 • Last read: 2 days ago │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🔷 Solo Leveling                    │ │
│ │ Chapter 245 • Last read: Today      │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌═════════════════════════════════════┐ │
│ ║ 🔷 The Beginning After The End   ◄──┼─┼── (Long pressed - highlighted)
│ ║ Chapter 175 • Last read: 1 day ago ║ │
│ ╚═════════════════════════════════════╝ │
│         ┌─────────────────────┐         │
│         │ ✅ Mark as Completed │         │
│         │ 🚫 Move to Dropped   │         │
│         │ 📋 Plan to Read      │         │
│         │ ℹ️ View Details      │         │
│         │ 🗑️ Delete Novel      │         │
│         └─────────────────────┘         │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🔷 Mushoku Tensei                   │ │
│ │ Chapter 89 • Last read: 5 days ago  │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🔷 That Time I Got Reincarnated... │ │
│ │ Chapter 98 • Last read: 1 week ago  │ │
│ └─────────────────────────────────────┘ │
│                                         │
└─────────────────────────────────────────┘
```

## Design Elements

### Novel List Item (Normal State)
- **Background**: White card with subtle shadow
- **Title**: Bold, dark text (16sp)
- **Chapter**: "Chapter X" in medium gray (14sp)
- **Last Read**: Light gray text (12sp)
- **Padding**: 16dp all around
- **Margin**: 8dp between cards

### Novel List Item (Long-Pressed State)
- **Background**: Light blue tint with elevated shadow
- **Border**: 2dp accent color border
- **Elevation**: Raised to show selection

### Popup Menu
- **Position**: Centered above the selected item
- **Background**: White with rounded corners (8dp radius)
- **Shadow**: Elevated drop shadow
- **Width**: 200dp
- **Item Height**: 48dp per menu item
- **Icons**: 24dp colored icons for each action
- **Text**: 14sp, dark gray
- **Dividers**: None (Material Design 3 style)

### Action Icons and Colors
- ✅ Mark as Completed: Green (#4CAF50)
- 🚫 Move to Dropped: Red (#F44336)
- 📋 Plan to Read: Orange (#FF9800)
- ℹ️ View Details: Blue (#2196F3)
- 🗑️ Delete Novel: Dark Red (#B71C1C)

## Interaction Flow

1. **User long-presses** on a novel item (hold for ~500ms)
2. **Visual feedback**: Item highlights with elevation and tint
3. **Popup appears** with smooth fade-in animation
4. **User taps** on an action
5. **Action executes** and popup dismisses
6. **List updates** to reflect the change

## Alternative Design: Bottom Sheet

For better mobile UX, consider using a bottom sheet instead:

```
┌─────────────────────────────────────────┐
│ 📚 Light Novel Reader                   │
│─────────────────────────────────────────│
│ (Novel list as above...)                │
│                                         │
│═════════════════════════════════════════│
│ The Beginning After The End             │
│ Chapter 175 • Last read: 1 day ago      │
│─────────────────────────────────────────│
│ ✅ Mark as Completed                    │
│─────────────────────────────────────────│
│ 🚫 Move to Dropped                     │
│─────────────────────────────────────────│
│ 📋 Plan to Read                        │
│─────────────────────────────────────────│
│ ℹ️ View Details                        │
│─────────────────────────────────────────│
│ 🗑️ Delete Novel                        │
└─────────────────────────────────────────┘
```

## Benefits of Long-Press Popup Over Swipe

1. **Discoverability**: Users expect long-press for context menus
2. **Multiple Actions**: Can show 5+ actions vs 2 swipe directions
3. **Visual Clarity**: User sees all options before choosing
4. **Accessibility**: Better for users with motor impairments
5. **No Accidental Actions**: Swipes can trigger unintentionally