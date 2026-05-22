package khiem.it.tinyenglish;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
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
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    // Đã sửa: Điều kiện đạt là đúng từ 10 câu trở lên
    private static final int REQUIRED_CORRECT_ANSWERS = 10;

    private String lessonId;
    private String lessonTitle;
    private List<Question> questions = new ArrayList<>();
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
        boolean usedInline = renderQuestionContent(question);
        questionCounterView.setText((currentQuestionIndex + 1) + " / " + questions.size());

        int progressPercent = (int) ((currentQuestionIndex + 1) * 100 / questions.size());
        progressPercentView.setText(progressPercent + "%");
        progressBar.setProgress(progressPercent);

        if (!usedInline) {
            loadQuestionImage(question.getImageUrl());
        }

        radioGroup.removeAllViews();
        checkBoxContainer.removeAllViews();

        if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
            displayMultipleChoice(question);
        } else if ("TRUE_FALSE".equals(question.getQuestionType())) {
            displayTrueFalse(question);
        }

        prevButton.setEnabled(currentQuestionIndex > 0);
        nextButton.setText(currentQuestionIndex == questions.size() - 1 ? "Hoàn thành" : "Tiếp");
    }

    private boolean renderQuestionContent(Question question) {
        String imageUrl = question.getImageUrl();
        if (isLocalResourceImage(imageUrl)) {
            CharSequence inlineContent = buildInlineQuestionContent(question.getQuestionText(), imageUrl);
            if (inlineContent != null) {
                questionTextView.setText(inlineContent);
                questionImage.setVisibility(View.GONE);
                return true;
            }
        }

        questionTextView.setText(question.getQuestionText());
        return false;
    }

    private boolean isLocalResourceImage(String imageUrl) {
        return imageUrl != null && imageUrl.startsWith("android.resource://");
    }

    private CharSequence buildInlineQuestionContent(String questionText, String imageUrl) {
        try {
            Uri uri = Uri.parse(imageUrl);
            List<String> pathSegments = uri.getPathSegments();
            if (pathSegments.size() < 2) {
                return null;
            }

            String resourceType = pathSegments.get(0);
            String resourceName = pathSegments.get(1);
            int resourceId = getResources().getIdentifier(resourceName, resourceType, getPackageName());
            if (resourceId == 0) {
                return null;
            }

            Drawable drawable = ContextCompat.getDrawable(this, resourceId);
            if (drawable == null) {
                return null;
            }

            int maxWidth = (int) (getResources().getDisplayMetrics().density * 240);
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            int targetWidth = intrinsicWidth > 0 ? Math.min(intrinsicWidth, maxWidth) : maxWidth;
            int targetHeight = intrinsicWidth > 0 && intrinsicHeight > 0
                    ? Math.max(1, (int) ((float) targetWidth * intrinsicHeight / intrinsicWidth))
                    : targetWidth;
            drawable.setBounds(0, 0, targetWidth, targetHeight);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append('\uFFFC');
            builder.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_CENTER), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append('\n').append(questionText);
            return builder;
        } catch (Exception e) {
            Log.e(TAG, "Failed to build inline question content", e);
            return null;
        }
    }

    private void loadQuestionImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            questionImage.setVisibility(View.GONE);
            return;
        }

        questionImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imageUrl)
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

        // Đạt nếu đúng >= 10 câu HOẶC đúng hết toàn bộ câu hỏi (phòng khi bài học ngắn có tổng số câu < 10)
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

    // Class Helper hỗ trợ ánh xạ ID và thứ tự hiển thị của bài học từ Firebase
    private static class LessonOrderHelper {
        String id;
        int order;

        LessonOrderHelper(String id, int order) {
            this.id = id;
            this.order = order;
        }
    }

    private void unlockNextLesson() {
        lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                // 1. Đọc toàn bộ danh sách bài học cùng thuộc tính "order" tương ứng
                List<LessonOrderHelper> lessonList = new ArrayList<>();
                for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                    String id = lessonSnapshot.getKey();
                    Integer order = lessonSnapshot.child("order").getValue(Integer.class);
                    if (order == null) {
                        order = 0;
                    }
                    lessonList.add(new LessonOrderHelper(id, order));
                }

                // 2. Sắp xếp danh sách bài học theo thứ tự tăng dần của trường "order"
                lessonList.sort((l1, l2) -> Integer.compare(l1.order, l2.order));

                // 3. Trích xuất mảng ID đã được sắp xếp chuẩn chuỗi tuyến tính 1 -> 2 -> 3
                List<String> sortedLessonIds = new ArrayList<>();
                for (LessonOrderHelper helper : lessonList) {
                    sortedLessonIds.add(helper.id);
                }

                // 4. Định vị index của bài học hiện tại để tìm ra ID bài kế tiếp
                int currentIndex = sortedLessonIds.indexOf(lessonId);

                if (currentIndex >= 0 && currentIndex < sortedLessonIds.size() - 1) {
                    String nextLessonId = sortedLessonIds.get(currentIndex + 1);

                    // 5. Khởi tạo bản ghi tiến độ cho bài học kế tiếp
                    // Đã sửa: passed = false để màn hình ngoài không nhận nhầm trạng thái hoàn thành
                    UserProgress nextLessonProgress = new UserProgress(
                            currentUser.getUid(),
                            nextLessonId,
                            0,
                            false,
                            System.currentTimeMillis()
                    );

                    progressRef.child(nextLessonId).setValue(nextLessonProgress)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Đã mở khóa chính xác bài học tiếp theo: " + nextLessonId);
                                } else {
                                    Log.e(TAG, "Lỗi mở khóa bài học tiếp theo: " + task.getException());
                                }
                            });
                } else {
                    Log.d(TAG, "Bạn đã hoàn thành bài học cuối cùng!");
                }

                // Điều hướng tự động quay lại màn hình danh sách sau 2 giây
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