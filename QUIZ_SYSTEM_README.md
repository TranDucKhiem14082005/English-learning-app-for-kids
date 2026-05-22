# TinyEnglish Quiz System - Setup Guide

## ✅ Hoàn tất (Completed)

### 1. **Model Classes** (Tạo 3 model classes mới)
- ✓ `Lesson.java`: Lưu trữ thông tin bài học (id, title, description, emoji, totalQuestions, unlocked)
- ✓ `Question.java`: Lưu trữ câu hỏi (id, lessonId, questionText, questionType, options, correctAnswer)
- ✓ `UserProgress.java`: Tracking tiến độ người dùng (userId, lessonId, score, completed, lastAttemptDate)

### 2. **Activity & UI Components**
- ✓ `LessonDetailActivity.java`: Activity chính để làm quiz
  - Hiển thị từng câu hỏi
  - Layout: câu hỏi + đáp án (radio button cho MULTIPLE_CHOICE, checkbox cho TRUE_FALSE)
  - Nút "Tiếp" và "Quay lại"
  - Tracking score hiện tại + progress bar
  - Khi hoàn thành: hiển thị kết quả (score/20, điểm %, pass/fail)
  - Pass >= 15/20: unlock bài học tiếp theo

- ✓ `LessonsFragment.java` (Updated):
  - Load lessons từ Firebase Realtime Database
  - Auto-seed dữ liệu nếu chưa có (5 lessons + 20 questions mỗi lesson)
  - Hiển thị danh sách qua RecyclerView

- ✓ `LessonAdapter.java` (Updated):
  - Hiển thị lesson card với emoji, status, score
  - Status: Locked/Ready/Completed
  - Click để mở LessonDetailActivity
  - Load progress từ Firebase

### 3. **Layouts**
- ✓ `activity_lesson_detail.xml`: Layout cho quiz
  - Progress bar
  - Question text
  - Options (RadioGroup + CheckBoxes)
  - Buttons (Previous, Next)

- ✓ `item_lesson.xml` (Updated):
  - Lesson card với emoji + title + status + score

- ✓ `fragment_lessons.xml` (Updated):
  - Gradient background
  - RecyclerView cho danh sách lessons

### 4. **Drawable Resources**
- ✓ `gradient_background.xml`: Gradient background (xanh-tím)
- ✓ `rounded_card_bg.xml`: Rounded background cho cards
- ✓ `status_badge.xml`: Badge background cho status
- ✓ `progress_bar_bg.xml`: Custom progress bar

### 5. **Color Resources** (Updated)
- ✓ Added colors: text_primary, text_secondary, card_bg, status_ready, status_locked, status_completed, secondary_gray

### 6. **Firebase Integration**
- ✓ Structure:
  ```
  /lessons/{lessonId}/
    - id, title, description, emoji, totalQuestions, unlocked
    - /questions/{questionIndex}
      - id, lessonId, questionText, questionType, options, correctAnswer, order
  
  /userProgress/{userId}/{lessonId}/
    - userId, lessonId, score, completed, lastAttemptDate
  ```

- ✓ Auto-seeded Data: 5 lessons với 20 câu hỏi mỗi cái
  - Lesson 1: Màu sắc (🎨) - UNLOCKED
  - Lesson 2: Động vật (🐾) - LOCKED
  - Lesson 3: Cây cỏ (🌿) - LOCKED
  - Lesson 4: Trái cây (🍎) - LOCKED
  - Lesson 5: Rau xanh (🥕) - LOCKED

### 7. **Logic Implementation**
- ✓ Answer checking: So sánh user answer vs correctAnswer
- ✓ Score tracking: Cộng điểm cho đáp án đúng
- ✓ Progress saving: Lưu userProgress sau mỗi lesson
- ✓ Unlock system: Unlock lesson tiếp theo khi pass (>= 15/20)
- ✓ Retry logic: Cho phép làm lại lesson

## 🚀 How to Use

### 1. **First Time Setup**
- Ứng dụng sẽ tự động seed dữ liệu vào Firebase lần đầu
- Kiểm tra Firebase Console để xem dữ liệu

### 2. **User Flow**
1. User đăng nhập → MainActivity
2. Click vào Lessons tab → LessonsFragment
3. Danh sách lessons hiển thị (Lesson 1 sẵn sàng, các bài khác bị khoá)
4. Click vào lesson → LessonDetailActivity
5. Trả lời 20 câu hỏi
6. Xem kết quả:
   - Pass (>= 15): Unlock bài tiếp theo
   - Fail (< 15): Cho phép làm lại

### 3. **Question Types**
- **MULTIPLE_CHOICE**: Radio buttons với 3-4 lựa chọn
- **TRUE_FALSE**: 2 radio buttons (True/False)

## 📱 Testing

### Manual Testing Steps
1. **Seed Data**:
   - Install app
   - Navigate to Lessons
   - Verify 5 lessons appear (Lesson 1 Unlocked, others Locked)

2. **Play Quiz**:
   - Click Lesson 1
   - Answer all 20 questions
   - Check score calculation

3. **Unlock System**:
   - Pass Lesson 1 (>= 15/20)
   - Return to Lessons
   - Verify Lesson 2 is now unlocked

4. **Retry Logic**:
   - Fail a lesson (< 15/20)
   - Should show retry dialog
   - Re-attempt from question 1

## 🐛 Debug Tips

### Firebase Rules
Ensure Firebase Realtime Database rules allow authenticated users:
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

### Log Tags
- `LessonDetailActivity`: Quiz gameplay
- `LessonsFragment`: Loading lessons
- `LessonAdapter`: Binding data
- `FirebaseDataSeeder`: Data seeding

## 📋 Files Created/Modified

### Created:
- `model/Question.java`
- `model/UserProgress.java`
- `LessonDetailActivity.java`
- `FirebaseDataSeeder.java`
- `layout/activity_lesson_detail.xml`
- `drawable/gradient_background.xml` (verified)
- `drawable/rounded_card_bg.xml`
- `drawable/status_badge.xml`
- `drawable/progress_bar_bg.xml`

### Modified:
- `model/Lesson.java`: Added new fields (id, emoji, unlocked)
- `ui/LessonsFragment.java`: Firebase integration + data seeding
- `adapter/LessonAdapter.java`: Progress loading + click handling
- `layout/fragment_lessons.xml`: Better styling
- `layout/item_lesson.xml`: New card layout with emoji, status, score
- `values/colors.xml`: Added quiz colors
- `AndroidManifest.xml`: Registered LessonDetailActivity

## ✨ Features

✓ 20-question quizzes per lesson
✓ Two question types (Multiple Choice, True/False)
✓ Auto-scoring and progress tracking
✓ Lock/Unlock system
✓ Emoji-based lesson icons
✓ Retry functionality
✓ Firebase Realtime Database integration
✓ Beautiful gradient UI
✓ Progress bar with percentage
✓ Material Design components
