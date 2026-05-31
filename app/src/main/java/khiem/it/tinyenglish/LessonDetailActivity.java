package khiem.it.tinyenglish;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import khiem.it.tinyenglish.model.Question;
import khiem.it.tinyenglish.model.UserProgress;

public class LessonDetailActivity extends AppCompatActivity {

    private static final String TAG = "LessonDetail";
    // Điều kiện đạt yêu cầu: Làm đúng từ 10 câu trở lên
    private static final int REQUIRED_CORRECT_ANSWERS = 10;

    private String lessonId;
    private String lessonTitle;
    private final List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    private TextView questionTextView;
    private TextView questionCounterView;
    private TextView progressPercentView;
    private ProgressBar progressBar;
    private RadioGroup radioGroup;
    private LinearLayout checkBoxContainer;
    private MaterialButton nextButton;
    private MaterialButton prevButton;
    private ImageView questionImage;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference lessonsRef;
    private DatabaseReference progressRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        firebaseDatabase = FirebaseDatabase.getInstance();
        lessonsRef = firebaseDatabase.getReference("lessons");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressRef = firebaseDatabase.getReference("userProgress").child(currentUser.getUid());

        initializeViews();
        getIntentData();
        loadQuestions();
    }

    private void initializeViews() {
        questionTextView = findViewById(R.id.questionText);
        questionCounterView = findViewById(R.id.questionCounter);
        progressPercentView = findViewById(R.id.progressPercent);
        progressBar = findViewById(R.id.progressBar);
        radioGroup = findViewById(R.id.optionsRadioGroup);
        checkBoxContainer = findViewById(R.id.checkBoxContainer);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        questionImage = findViewById(R.id.questionImage);

        nextButton.setOnClickListener(v -> handleNextQuestion());
        prevButton.setOnClickListener(v -> handlePrevQuestion());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        lessonId = intent.getStringExtra("lessonId");
        lessonTitle = intent.getStringExtra("lessonTitle");
        setTitle(lessonTitle);
    }

    private void loadQuestions() {
        lessonsRef.child(lessonId).child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questions.clear();
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    Question question = questionSnapshot.getValue(Question.class);
                    if (question != null) {
                        questions.add(question);
                    }
                }

                if (!questions.isEmpty()) {
                    questions.sort((q1, q2) -> Integer.compare(q1.getOrder(), q2.getOrder()));
                    displayQuestion();
                } else {
                    Toast.makeText(LessonDetailActivity.this, "No questions found", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No questions found for lesson: " + lessonId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load questions: " + error.getMessage());
                Toast.makeText(LessonDetailActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResults();
            return;
        }

        Question question = questions.get(currentQuestionIndex);

        // Cập nhật thông tin Tiến độ chung (Dùng chung cho tất cả các loại câu hỏi)
        questionCounterView.setText((currentQuestionIndex + 1) + " / " + questions.size());
        int progressPercent = (int) ((currentQuestionIndex + 1) * 100 / questions.size());
        progressPercentView.setText(progressPercent + "%");
        progressBar.setProgress(progressPercent);

        radioGroup.removeAllViews();
        checkBoxContainer.removeAllViews();

        // LUỒNG XỬ LÝ RẼ NHÁNH TỐI ƯU KHÔNG LẶP CODE
        if ("GUIDED_TRANSLATION".equals(question.getQuestionType())) {
            // Nhánh mới bổ sung: Dành cho bài luyện dịch câu dịch (Tự quản lý text và ẩn ảnh)
            displayGuidedTranslation(question);
        } else {
            // GIỮ NGUYÊN: Nhánh dành cho 5 chủ đề cũ (Nạp chữ thuần túy và hiển thị ảnh qua Glide)
            questionTextView.setText(question.getQuestionText());
            loadQuestionImage(question.getImageUrl());

            if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
                displayMultipleChoice(question);
            } else if ("TRUE_FALSE".equals(question.getQuestionType())) {
                displayTrueFalse(question);
            }
        }

        prevButton.setEnabled(currentQuestionIndex > 0);
        nextButton.setText(currentQuestionIndex == questions.size() - 1 ? "Hoàn thành" : "Tiếp");
    }

    private void displayGuidedTranslation(Question question) {
        // Ẩn ImageView vì bài dịch câu tập trung hoàn toàn vào xử lý chữ trực quan
        questionImage.setVisibility(View.GONE);

        String rawText = question.getQuestionText();
        String formattedContent = rawText;

        if (rawText != null && rawText.contains("|")) {
            String[] parts = rawText.split("\\|");
            String vietnamese = parts[0];
            String englishHint = parts[1];

            // Hiển thị thuần nội dung câu hỏi và gợi ý, không kèm level hoặc icon
            formattedContent = "Câu hỏi:\n" + vietnamese + "\n\n" +
                    "Gợi ý:\n" + englishHint;
        }

        questionTextView.setText(formattedContent);

        // Đổ danh sách đáp án dưới dạng các ô lựa chọn RadioButton
        radioGroup.setVisibility(View.VISIBLE);
        checkBoxContainer.setVisibility(View.GONE);

        if (question.getOptions() != null) {
            for (String option : question.getOptions()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(option);
                radioButton.setTag(option);
                radioButton.setId(View.generateViewId());
                radioButton.setPadding(0, 16, 0, 16);
                radioGroup.addView(radioButton);
            }
        }
    }

    private void loadQuestionImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            questionImage.setVisibility(View.GONE);
            return;
        }

        questionImage.setVisibility(View.VISIBLE);

        // Sử dụng Glide thông minh: Tự động nhận diện cả link Web lẫn đường dẫn android.resource:// để căn giữa fitCenter
        Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .placeholder(R.drawable.rounded_card_bg)
                .error(R.drawable.rounded_card_bg)
                .into(questionImage);
    }

    private void displayMultipleChoice(Question question) {
        radioGroup.setVisibility(View.VISIBLE);
        checkBoxContainer.setVisibility(View.GONE);

        for (String option : question.getOptions()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioButton.setTag(option);
            radioButton.setId(View.generateViewId());
            radioButton.setPadding(0, 16, 0, 16);
            radioGroup.addView(radioButton);
        }
    }

    private void displayTrueFalse(Question question) {
        radioGroup.setVisibility(View.VISIBLE);
        checkBoxContainer.setVisibility(View.GONE);

        RadioButton trueButton = new RadioButton(this);
        trueButton.setText("Đúng");
        trueButton.setTag("True");
        trueButton.setId(View.generateViewId());
        trueButton.setPadding(0, 16, 0, 16);
        radioGroup.addView(trueButton);

        RadioButton falseButton = new RadioButton(this);
        falseButton.setText("Sai");
        falseButton.setTag("False");
        falseButton.setId(View.generateViewId());
        falseButton.setPadding(0, 16, 0, 16);
        radioGroup.addView(falseButton);
    }

    private void handleNextQuestion() {
        if (currentQuestionIndex >= questions.size()) return;

        Question question = questions.get(currentQuestionIndex);
        String userAnswer = getSelectedAnswer();

        if (userAnswer == null) {
            Toast.makeText(this, "Vui lòng chọn đáp án", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userAnswer.equals(question.getCorrectAnswer())) {
            score++;
            showCorrectExplanation(question);
        }

        currentQuestionIndex++;
        displayQuestion();
    }

    private void handlePrevQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayQuestion();
        }
    }

    private void showCorrectExplanation(Question question) {
        String explanation = question.getExplanation();
        if (explanation == null || explanation.trim().isEmpty()) {
            return;
        }
        Toast.makeText(this, explanation, Toast.LENGTH_LONG).show();
    }

    private String getSelectedAnswer() {
        if (radioGroup.getVisibility() == View.VISIBLE) {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadio = radioGroup.findViewById(selectedId);
                if (selectedRadio != null) {
                    return (String) selectedRadio.getTag();
                }
            }
        } else if (checkBoxContainer.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < checkBoxContainer.getChildCount(); i++) {
                View child = checkBoxContainer.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    if (checkBox.isChecked()) {
                        return (String) checkBox.getTag();
                    }
                }
            }
        }
        return null;
    }

    private void showResults() {
        int totalQuestions = Math.max(questions.size(), 1);
        int percentage = (score * 100) / totalQuestions;

        boolean passed = score >= REQUIRED_CORRECT_ANSWERS || score == totalQuestions;

        String resultText = "Kết quả: " + score + "/" + totalQuestions + "\n" +
                "Điểm: " + percentage + "%\n" +
                (passed ? "✓ Đạt yêu cầu! Bài học tiếp theo sẽ được mở khóa." :
                        "Xin chia buồn, bạn cần đúng ít nhất " + Math.min(REQUIRED_CORRECT_ANSWERS, totalQuestions) + " câu để đạt.");

        Toast.makeText(this, resultText, Toast.LENGTH_LONG).show();

        saveProgress(score, passed);
    }

    private void saveProgress(int finalScore, boolean passed) {
        String progressPath = lessonId;
        UserProgress progress = new UserProgress(
                currentUser.getUid(),
                lessonId,
                finalScore,
                passed,
                System.currentTimeMillis()
        );

        progressRef.child(progressPath).setValue(progress).addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) return;

            if (task.isSuccessful()) {
                Log.d(TAG, "Progress saved successfully");

                if (passed) {
                    unlockNextLesson();
                } else {
                    nextButton.postDelayed(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        AlertDialog dialog = new AlertDialog.Builder(LessonDetailActivity.this)
                                .setTitle("Làm lại bài kiểm tra")
                                .setMessage("Bạn có muốn làm lại bài này không?")
                                .setPositiveButton("Có", (d, which) -> retakeQuiz())
                                .setNegativeButton("Không", (d, which) -> finish())
                                .setCancelable(false)
                                .create();
                        dialog.show();
                    }, 1000);
                }
            } else {
                Log.e(TAG, "Failed to save progress: " + task.getException());
            }
        });
    }

    private static class LessonOrderHelper {
        String id;
        int order;

        LessonOrderHelper(String id, int order) {
            this.id = id;
            this.order = order;
        }
    }

    private void unlockNextLesson() {
        // 1. Duyệt thẳng qua toàn bộ danh sách bài học trên Firebase để tìm bài tiếp theo dựa trên chuỗi Emoji thống nhất
        lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                // Chuỗi định vị thứ tự bài học chuẩn chỉnh xuyên suốt hệ thống
                String orderStr = "🎨🐾🌿🍎🥕📝";

                // Lấy thông tin Emoji của bài học hiện tại để biết mình đang đứng ở đâu
                DataSnapshot currentLessonSnapshot = snapshot.child(lessonId);
                String currentEmoji = currentLessonSnapshot.child("emoji").getValue(String.class);

                if (currentEmoji == null) currentEmoji = "";
                int currentIdx = orderStr.indexOf(currentEmoji);
                int nextIdxTarget = currentIdx + 1; // Vị trí của bài tiếp theo cần tìm

                String tempNextLessonId = null;

                // 2. Tìm xem bài học nào trên Firebase có chứa Emoji nằm ở vị trí nextIdxTarget
                for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                    String emoji = lessonSnapshot.child("emoji").getValue(String.class);
                    if (emoji != null && orderStr.indexOf(emoji) == nextIdxTarget) {
                        tempNextLessonId = lessonSnapshot.getKey(); // Tìm thấy ID bài tiếp theo phù hợp!
                        break;
                    }
                }

                // Khóa cố định biến trung gian để mang vào khối lệnh Lambda an toàn
                final String nextLessonId = tempNextLessonId;

                // 3. Tiến hành tạo bản ghi mở khóa trên Firebase nếu tìm thấy bài học tiếp theo
                if (nextLessonId != null) {
                    UserProgress nextLessonProgress = new UserProgress(
                            currentUser.getUid(),
                            nextLessonId,
                            0,
                            false,
                            System.currentTimeMillis()
                    );

                    // Đánh dấu mở khóa bằng cách set bản ghi trạng thái rỗng vào node userProgress
                    progressRef.child(nextLessonId).setValue(nextLessonProgress)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Đã mở khóa chính xác bài học tiếp theo: " + nextLessonId);
                                } else {
                                    Log.e(TAG, "Lỗi mở khóa bài học tiếp theo: " + task.getException());
                                }
                            });
                } else {
                    Log.d(TAG, "Chúc mừng! Bạn đã hoàn thành bài học cuối cùng của khóa học.");
                }

                // Quay trở lại màn hình danh sách bài học sau khi hoàn thành
                nextButton.postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        finish();
                    }
                }, 2000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get lessons list: " + error.getMessage());
            }
        });
    }

    private void retakeQuiz() {
        currentQuestionIndex = 0;
        score = 0;
        radioGroup.clearCheck();
        checkBoxContainer.removeAllViews();
        displayQuestion();
    }
}
