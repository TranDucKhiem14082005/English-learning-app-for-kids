# Quiz System Testing Guide

## 🧪 Pre-Testing Checklist

### 1. Firebase Console Setup
- [ ] Go to Firebase Console
- [ ] Select your TinyEnglish project
- [ ] Check Realtime Database
- [ ] Set Rules to allow reads for authenticated users:

```json
{
  "rules": {
    "lessons": {
      ".read": true
    },
    "userProgress": {
      "$uid": {
        ".read": "auth.uid == $uid",
        ".write": "auth.uid == $uid"
      }
    }
  }
}
```

### 2. Code Build Check
All necessary files have been created:
- ✓ Model classes: Lesson, Question, UserProgress
- ✓ LessonDetailActivity with quiz logic
- ✓ Updated LessonsFragment with Firebase integration
- ✓ Updated LessonAdapter with progress loading
- ✓ All layout files (activity_lesson_detail.xml, item_lesson.xml, fragment_lessons.xml)
- ✓ All drawable resources (gradients, badges, progress bar)
- ✓ Updated AndroidManifest.xml with new activity

## 🚀 Testing Scenarios

### Scenario 1: First Launch & Data Seeding
**Expected Behavior:**
1. Launch app
2. User logs in successfully
3. Navigate to Lessons tab
4. Should see 5 lesson cards in RecyclerView

**Verification:**
- [ ] Firebase /lessons contains 5 lesson entries
- [ ] Each lesson has 20 questions under /lessons/{lessonId}/questions
- [ ] Lesson 1 (Màu sắc) shows "Sẵn sàng"
- [ ] Lessons 2-5 show "Khoá"
- [ ] Score shows "0/20" for all lessons

**Firebase Structure Check:**
```
lessons
├── lesson_colors
│   ├── id: "lesson_colors"
│   ├── title: "Bài 1: Màu sắc"
│   ├── emoji: "🎨"
│   ├── totalQuestions: 20
│   ├── unlocked: true
│   └── questions
│       ├── 0: {Question object}
│       ├── 1: {Question object}
│       └── ... (20 total)
├── lesson_animals
│   └── ... (unlocked: false)
└── ...
```

### Scenario 2: Playing a Quiz
**Steps:**
1. Tap on "Bài 1: Màu sắc"
2. LessonDetailActivity opens

**Verification:**
- [ ] Question 1/20 displays with emoji
- [ ] Progress bar shows 5%
- [ ] RadioGroup contains options (or CheckBox for TRUE_FALSE)
- [ ] Previous button is disabled
- [ ] Next button is enabled
- [ ] Question text is readable

**While Playing:**
- [ ] Answer each question
- [ ] Progress bar updates after each Next click
- [ ] Counter shows correct position (e.g., "5/20")
- [ ] Can go back to previous questions with Previous button
- [ ] Cannot modify answers after moving forward (current implementation)

### Scenario 3: Passing a Quiz (Score >= 15)
**Setup:** Complete Lesson 1 with correct answers (18-20 correct)

**Expected Behavior:**
1. After question 20, show results toast
2. Result shows: "Kết quả: 18/20\nĐiểm: 90%\n✓ Đạt yêu cầu!..."
3. Activity closes after 2 seconds
4. Lesson 2 (Động vật) becomes unlocked

**Verification:**
- [ ] userProgress/{userId}/lesson_colors contains:
  - score: 18
  - completed: true
  - lastAttemptDate: current timestamp
- [ ] Return to Lessons
- [ ] Lesson 2 now shows "Sẵn sàng"
- [ ] Lesson 2 score shows "0/20" initially

### Scenario 4: Failing a Quiz (Score < 15)
**Setup:** Complete Lesson 1 with few correct answers (10-14)

**Expected Behavior:**
1. Results show: "Kết quả: 12/20\nĐiểm: 60%\n✗ Chưa đạt yêu cầu..."
2. AlertDialog appears: "Làm lại bài kiểm tra?" with "Có/Không" buttons
3. If "Có": Quiz restarts from question 1 (score resets to 0)
4. If "Không": Return to LessonsFragment

**Verification:**
- [ ] userProgress/{userId}/lesson_colors contains:
  - score: 12
  - completed: false
  - lastAttemptDate: current timestamp
- [ ] Lesson 2 remains locked
- [ ] Can retry Lesson 1 multiple times

### Scenario 5: Retrying a Failed Quiz
**Setup:** Fail Lesson 1 (score < 15), then select "Có" in retry dialog

**Expected Behavior:**
1. Quiz resets to question 1
2. Progress bar resets to 5%
3. Counter shows "1/20"
4. All RadioButtons are unchecked
5. Score counter resets internally to 0

**Verification:**
- [ ] Answer questions again
- [ ] Complete with score >= 15
- [ ] Lesson 2 now unlocks

### Scenario 6: Locked Lessons
**Steps:**
1. Return to Lessons
2. Tap on Lesson 2 (locked)

**Expected Behavior:**
- [ ] Toast shows "Lesson bị khoá. Hoàn thành bài học trước đó!"
- [ ] LessonDetailActivity does not open

### Scenario 7: Multiple Lessons Chain
**Setup:** Complete all 5 lessons successfully

**Expected Behavior:**
1. Lesson 1 → Pass → Lesson 2 unlocks
2. Lesson 2 → Pass → Lesson 3 unlocks
3. ... continue ...
4. After Lesson 5 completes, no crash when trying to unlock non-existent Lesson 6

**Verification:**
- [ ] All lessons show "✓ Hoàn thành"
- [ ] All show their scores (e.g., "Score: 18/20")
- [ ] No Firebase errors in logcat

## 📊 Question Types Testing

### Multiple Choice Questions
**Expected:**
- RadioGroup with 3-4 options
- Mutually exclusive selection
- Selected radio button shows visual feedback

### True/False Questions
**Expected:**
- RadioGroup with 2 options ("Đúng", "Sai")
- Same radio button behavior as multiple choice

## 🔍 Debug Steps

### Check Logcat for Errors
```
adb logcat -s "LessonDetail|LessonsFragment|LessonAdapter|FirebaseDataSeeder"
```

### Firebase Console
- Check Realtime Database for structure
- Monitor userProgress/{userId} updates
- Look for failed write operations

### Common Issues

**Issue: Lessons don't load**
- [ ] Check Firebase connection
- [ ] Verify database rules
- [ ] Check if user is authenticated

**Issue: Questions don't appear**
- [ ] Verify /lessons/{lessonId}/questions exists
- [ ] Check logcat for "No questions found" message
- [ ] Ensure question IDs are correctly formatted

**Issue: Score not saving**
- [ ] Check Firebase write permissions
- [ ] Verify userProgress path: `/userProgress/{uid}/{lessonId}`
- [ ] Check for exceptions in logcat

**Issue: Unlock system not working**
- [ ] Verify passing score is >= 15
- [ ] Check if current lesson ID is correct
- [ ] Ensure next lesson exists before trying to unlock

## ✅ Final Verification Checklist

- [ ] App builds without errors
- [ ] App runs on emulator/device
- [ ] Firebase connection works (check in logcat)
- [ ] Data seeds on first launch
- [ ] Can play Lesson 1 (unlocked by default)
- [ ] Can answer all 20 questions
- [ ] Results show correctly (score, percentage)
- [ ] Can pass and unlock Lesson 2
- [ ] Can fail and retry
- [ ] Other lessons remain locked until previous completed
- [ ] No crashes during any operation
- [ ] Firebase data updates correctly
- [ ] UI is responsive during loading

## 📱 Test Data

**Lesson 1: Màu sắc (Colors)**
- Q1: "Màu gì có mã hex #FF0000?" → "Đỏ"
- Q2: "Lá cây thường màu gì?" → "Xanh"
- Q3: "Bầu trời là màu gì?" → "Xanh dương"
- Q4: "Tuyết thường là trắng" (TRUE/FALSE) → "True"
- Q5: "Biển là màu xanh lá cây" (TRUE/FALSE) → "False"
- Q6-Q20: Generic questions

To pass with high score: Answer Q1-Q5 correctly + at least 10 of Q6-Q20

## 🎯 Success Criteria

✅ All 5 lessons appear in list
✅ Can play unlocked lessons  
✅ Cannot play locked lessons
✅ Score calculation is correct
✅ Pass/fail logic works (>= 15)
✅ Unlock system chains lessons properly
✅ Retry functionality works
✅ Firebase data persists correctly
✅ UI is responsive and beautiful
✅ No crashes or ANRs
