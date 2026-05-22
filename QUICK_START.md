# 🚀 Quick Start Guide - TinyEnglish Quiz System

## ⚡ 5-Minute Setup

### 1. Sync Gradle
```bash
./gradlew sync
```

### 2. Build APK
```bash
./gradlew build --no-daemon
```

### 3. Run on Device/Emulator
```bash
./gradlew installDebug
```

## 📱 First Launch

1. **App opens** → Shows login/register
2. **Sign in** → Navigates to MainActivity
3. **Tap "Lessons"** → LessonsFragment loads
4. **Data seeds** → Firebase creates 5 lessons + 100 questions automatically
5. **View lessons** → Lesson 1 (Colors) shows as "Sẵn sàng"
6. **Tap Lesson 1** → Opens LessonDetailActivity
7. **Answer questions** → 20 questions to complete
8. **Submit answers** → See results
9. **Pass (≥15)** → Lesson 2 unlocks automatically

## 📊 Expected Output

### Firebase Console
After first launch, you should see:
```
Realtime Database
├── lessons
│   ├── lesson_colors
│   │   ├── id: "lesson_colors"
│   │   ├── title: "Bài 1: Màu sắc"
│   │   ├── emoji: "🎨"
│   │   ├── unlocked: true
│   │   └── questions (20 items)
│   ├── lesson_animals { unlocked: false }
│   ├── lesson_plants { unlocked: false }
│   ├── lesson_fruits { unlocked: false }
│   └── lesson_vegetables { unlocked: false }
└── userProgress
    └── {userId}
        └── lesson_colors { score: 18, completed: true }
```

### Logcat Output (Debug)
```
LessonsFragment: No lessons found, seeding data...
LessonsFragment: Seeded 5 lessons
LessonDetailActivity: Question loaded: q1
LessonDetailActivity: Answer correct! Score: 1/1
LessonDetailActivity: Progress saved successfully
LessonDetailActivity: Next lesson unlocked: lesson_animals
```

## 🎮 User Interactions

### Play a Quiz
1. Click lesson card
2. Read question
3. Select option(s)
4. Click "Tiếp" (Next)
5. Repeat for 20 questions
6. View results

### Retry a Failed Quiz
1. Get score < 15/20
2. Click "Có" in retry dialog
3. Quiz resets to Q1
4. Try again

### Unlock Lessons
1. Complete Lesson 1 with score ≥ 15/20
2. Return to Lessons tab
3. Lesson 2 now shows "Sẵn sàng"
4. Can click to play

## 🐛 Debugging

### Check Firebase Connection
```bash
adb logcat | grep Firebase
```
Look for successful reads/writes

### Check Quiz Logic
```bash
adb logcat | grep LessonDetail
```
Should show question loading and scoring

### Check Adapter Issues
```bash
adb logcat | grep LessonAdapter
```
Should show progress loading

## ⚠️ Troubleshooting

### Issue: Firebase data not loading
- [ ] Check Firebase rules allow reads
- [ ] Verify user is authenticated
- [ ] Check network connectivity

### Issue: Questions not appearing
- [ ] Ensure /lessons/{id}/questions exists in Firebase
- [ ] Check question JSON structure in Firebase Console
- [ ] Look for "No questions found" in logcat

### Issue: Progress not saving
- [ ] Verify Firebase rules allow writes
- [ ] Check user is authenticated
- [ ] Ensure userProgress path is correct: /userProgress/{uid}/{lessonId}

### Issue: Lessons don't unlock
- [ ] Check score is >= 15
- [ ] Verify next lesson exists
- [ ] Look for unlock errors in logcat

### Issue: App crashes
- [ ] Check logcat for full stack trace
- [ ] Ensure all model classes have no-arg constructors
- [ ] Verify all layout IDs are correct

## 📈 Performance Tips

1. **Network**: Data loads on first launch, subsequent loads are faster
2. **Memory**: Quiz activity loads one question at a time
3. **Battery**: No background processes, saves on app close

## 🎯 Next Steps

1. **Testing**: Follow TESTING_GUIDE.md for comprehensive testing
2. **Customization**: 
   - Edit seed data in LessonsFragment.generateQuestions()
   - Modify colors in values/colors.xml
   - Update emojis in seedLessons()
3. **Deployment**: Build release APK with `./gradlew assembleRelease`

## 📚 File Reference

**Key Files to Know:**
- `LessonDetailActivity.java` - Quiz logic
- `LessonsFragment.java` - Data loading & seeding
- `LessonAdapter.java` - Lesson list display
- `activity_lesson_detail.xml` - Quiz UI
- `item_lesson.xml` - Lesson card UI

**Configuration Files:**
- `AndroidManifest.xml` - Activity registration
- `colors.xml` - Color definitions
- `strings.xml` - Text resources

## ✨ Pro Tips

1. **Data Seeding**: Only seeds on first launch. To reseed:
   ```
   1. Delete /lessons from Firebase Console
   2. Restart app
   3. New data will be seeded
   ```

2. **Testing Multiple Users**: Each user gets their own /userProgress/{uid} path

3. **Checking Scores**: In Firebase Console, see actual scores in /userProgress

4. **Adding Questions**: Edit LessonsFragment.generateQuestions() method

5. **Changing Passing Score**: Edit `PASSING_SCORE = 15` in LessonDetailActivity

## 🎓 Learning Resources

- Firebase Realtime Database: https://firebase.google.com/docs/database
- RecyclerView: https://developer.android.com/guide/topics/ui/layout/recyclerview
- Material Design: https://material.io/design
- Gradle: https://gradle.org/

## 📞 Support

For issues or questions:
1. Check logcat for error messages
2. Review TESTING_GUIDE.md
3. Check Firebase Console for data structure
4. Verify all files are created correctly

---

**Happy Learning! 🎉**
