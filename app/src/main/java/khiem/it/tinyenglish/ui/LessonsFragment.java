package khiem.it.tinyenglish.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.adapter.LessonAdapter;
import khiem.it.tinyenglish.model.Lesson;
import khiem.it.tinyenglish.model.Question;

public class LessonsFragment extends Fragment {

    private static final String TAG = "LessonsFragment";
    private RecyclerView recyclerView;
    private LessonAdapter adapter;
    private final List<Lesson> lessons = new ArrayList<>();
    private DatabaseReference lessonsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lessons, container, false);

        recyclerView = view.findViewById(R.id.lessonsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        lessonsRef = FirebaseDatabase.getInstance().getReference("lessons");

        loadLessons();

        return view;
    }

    private void loadLessons() {
        lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lessons.clear();

                if (snapshot.getChildrenCount() == 0) {
                    Log.d(TAG, "No lessons found, seeding data...");
                    seedLessons();
                } else {
                    for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                        Lesson lesson = lessonSnapshot.getValue(Lesson.class);
                        if (lesson != null) {
                            // ĐÃ SỬA: Ép cứng hiển thị tổng số câu hỏi luôn là 15 câu khi hiển thị ở màn hình ngoài
                            lesson.setTotalQuestions(15);
                            lessons.add(lesson);
                        }
                    }
                    lessons.sort((l1, l2) -> {
                        String order = "🎨🐾🌿🍎🥕";
                        int idx1 = order.indexOf(l1.getEmoji());
                        int idx2 = order.indexOf(l2.getEmoji());
                        return Integer.compare(idx1, idx2);
                    });
                    updateAdapter();
                    syncQuestionContent();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load lessons: " + error.getMessage());
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load lessons", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void seedLessons() {
        Map<String, Lesson> lessonMap = new HashMap<>();
        // Tất cả đã được khởi tạo chuẩn chỉnh với 15 câu hỏi
        lessonMap.put("lesson_colors", new Lesson("lesson_colors", "Bài 1: Màu sắc", "Học tên các màu sắc cơ bản", "🎨", 15, true));
        lessonMap.put("lesson_animals", new Lesson("lesson_animals", "Bài 2: Động vật", "Nhận biết tên các loài động vật", "🐾", 15, false));
        lessonMap.put("lesson_plants", new Lesson("lesson_plants", "Bài 3: Cây cỏ", "Tìm hiểu về các loài cây", "🌿", 15, false));
        lessonMap.put("lesson_fruits", new Lesson("lesson_fruits", "Bài 4: Trái cây", "Học tên các loại trái cây", "🍎", 15, false));
        lessonMap.put("lesson_vegetables", new Lesson("lesson_vegetables", "Bài 5: Rau xanh", "Nhận diện các loại rau xanh", "🥕", 15, false));

        for (Map.Entry<String, Lesson> entry : lessonMap.entrySet()) {
            String lessonId = entry.getKey();
            Lesson lesson = entry.getValue();

            lessonsRef.child(lessonId).setValue(lesson).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Lesson seeded: " + lessonId);
                    seedQuestionsForLesson(lessonId);
                } else {
                    Log.e(TAG, "Failed to seed lesson: " + lessonId);
                }
            });
        }

        lessons.clear();
        lessons.addAll(lessonMap.values());
        lessons.sort((l1, l2) -> {
            String order = "🎨🐾🌿🍎🥕";
            int idx1 = order.indexOf(l1.getEmoji());
            int idx2 = order.indexOf(l2.getEmoji());
            return Integer.compare(idx1, idx2);
        });
        updateAdapter();
    }

    private void seedQuestionsForLesson(String lessonId) {
        DatabaseReference questionsRef = lessonsRef.child(lessonId).child("questions");
        List<Question> questions = generateQuestions(lessonId);

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            q.setOrder(i);
            q.setLessonId(lessonId);
            questionsRef.child(String.valueOf(i)).setValue(q).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to seed question for lesson: " + lessonId);
                }
            });
        }

        Log.d(TAG, "Seeded " + questions.size() + " questions for lesson: " + lessonId);
    }

    private List<Question> generateQuestions(String lessonId) {
        List<Question> questions = new ArrayList<>();

        if ("lesson_colors".equals(lessonId)) {
            String questionText = "What color is shown in the image?";
            String[] colorAnswers = new String[] {
                    "Yellow", "Black", "Purple", "Orange", "Brown",
                    "Gray", "Cyan", "Navy Blue", "Beige", "Magenta / Plum",
                    "Olive Green", "Maroon / Burgundy", "Metallic Gold", "Peach / Light Salmon", "Pink"
            };
            String[] colorImages = new String[] {
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q1",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q2",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q3",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q4",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q5",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q6",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q7",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q8",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q9",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q10",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q11",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q12",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q13",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q14",
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q15"
            };
            List<List<String>> colorOptions = Arrays.asList(
                    Arrays.asList("Yellow", "Green", "Orange", "Pink"),
                    Arrays.asList("Black", "White", "Gray", "Brown"),
                    Arrays.asList("Purple", "Pink", "Violet", "Blue"),
                    Arrays.asList("Orange", "Red", "Yellow", "Brown"),
                    Arrays.asList("Brown", "Beige", "Black", "Gray"),
                    Arrays.asList("Gray", "Silver", "White", "Black"),
                    Arrays.asList("Cyan", "Blue", "Teal", "Purple"),
                    Arrays.asList("Navy Blue", "Dark Blue", "Black", "Indigo"),
                    Arrays.asList("Beige", "Cream", "Brown", "Yellow"),
                    Arrays.asList("Magenta / Plum", "Pink", "Purple", "Red"),
                    Arrays.asList("Olive Green", "Lime Green", "Mint Green", "Emerald Green"),
                    Arrays.asList("Maroon / Burgundy", "Red", "Brown", "Gold"),
                    Arrays.asList("Metallic Gold", "Yellow", "Bronze", "Silver"),
                    Arrays.asList("Peach / Light Salmon", "Pink", "Orange", "Beige"),
                    Arrays.asList("Pink", "Purple", "Red", "Peach / Light Salmon")
            );
            String[] colorExplanations = new String[] {
                    "Yellow — bright like sunlight.", "Black — the darkest color, like night or charcoal.",
                    "Purple — a regal hue made by mixing red and blue.", "Orange — vivid and warm, like ripe oranges.",
                    "Brown — earthy and warm, like wood or soil.", "Gray — a neutral tone between black and white.",
                    "Cyan — a light blue-green, like tropical waters.", "Navy Blue — a deep, dark blue often used for uniforms.",
                    "Beige — a pale sandy tan color.", "Magenta / Plum — a deep pinkish-purple tone.",
                    "Olive Green — a muted green like olive leaves.", "Maroon / Burgundy — a dark reddish wine color.",
                    "Metallic Gold — shiny and metallic, like the precious metal.", "Peach / Light Salmon — a soft pinkish-orange shade.",
                    "Pink — a light red tone often associated with softness."
            };
            for (int i = 0; i < colorImages.length; i++) {
                questions.add(createColorQuestion("q" + (i + 1), questionText,
                        colorOptions.get(i), colorAnswers[i], colorImages[i], colorExplanations[i]));
            }
        } else if ("lesson_animals".equals(lessonId)) {
            String[] animalImages = new String[] {
                    "android.resource://khiem.it.tinyenglish/mipmap/cat",
                    "android.resource://khiem.it.tinyenglish/mipmap/dog",
                    "android.resource://khiem.it.tinyenglish/mipmap/elephant",
                    "android.resource://khiem.it.tinyenglish/mipmap/fox",
                    "android.resource://khiem.it.tinyenglish/mipmap/giraffe",
                    "android.resource://khiem.it.tinyenglish/mipmap/kangaroo",
                    "android.resource://khiem.it.tinyenglish/mipmap/koala",
                    "android.resource://khiem.it.tinyenglish/mipmap/lion",
                    "android.resource://khiem.it.tinyenglish/mipmap/monkey",
                    "android.resource://khiem.it.tinyenglish/mipmap/panda",
                    "android.resource://khiem.it.tinyenglish/mipmap/penguin",
                    "android.resource://khiem.it.tinyenglish/mipmap/polarbear",
                    "android.resource://khiem.it.tinyenglish/mipmap/rabbit",
                    "android.resource://khiem.it.tinyenglish/mipmap/squirrel",
                    "android.resource://khiem.it.tinyenglish/mipmap/tiger",

            };
            String[] animalAnswers = new String[] {
                    "Cat", "Dog", "Elephant", "Fox", "Giraffe",
                    "Kangaroo", "Koala", "Lion", "Monkey", "Panda",
                    "Penguin", "Polar Bear", "Rabbit", "Squirrel", "Tiger"
            };
            List<List<String>> animalOptions = Arrays.asList(
                    // 1. cat
                    Arrays.asList("Cat", "Fox", "Lion", "Panda"),
                    // 2. dog
                    Arrays.asList("Cat", "Rabbit", "Dog", "Squirrel"),
                    // 3. elephant
                    Arrays.asList("Elephant", "Giraffe", "Penguin", "Monkey"),
                    // 4. fox
                    Arrays.asList("Tiger", "Fox", "Monkey", "Dog"),
                    // 5. giraffe
                    Arrays.asList("Kangaroo", "Giraffe", "Elephant", "Lion"),
                    // 6. kangaroo
                    Arrays.asList("Koala", "Lion", "Kangaroo", "Elephant"),
                    // 7. koala
                    Arrays.asList("Koala", "Panda", "Kangaroo", "Giraffe"),
                    // 8. lion
                    Arrays.asList("Tiger", "Elephant", "Lion", "Koala"),
                    // 9. monkey
                    Arrays.asList("Squirrel", "Fox", "Rabbit", "Monkey"),
                    // 10. panda
                    Arrays.asList("Polar Bear", "Koala", "Panda", "Squirrel"),
                    // 11. penguin
                    Arrays.asList("Ostrich", "Penguin", "Seagull", "Swan"),
                    // 12. polarbear
                    Arrays.asList("Panda", "Bear", "Polar Bear", "Koala"),
                    // 13. rabbit
                    Arrays.asList("Monkey", "Rabbit", "Kangaroo", "Tiger"),
                    // 14. squirrel
                    Arrays.asList("Rabbit", "Squirrel", "Fox", "Cat"),
                    // 15. tiger
                    Arrays.asList("Tiger", "Leopard", "Fox", "Bear")
            );
            for (int i = 0; i < animalImages.length; i++) {
                questions.add(createQuestion("q" + (i + 1), "Which animal is shown in the image?", "MULTIPLE_CHOICE",
                        animalOptions.get(i), animalAnswers[i], animalImages[i]));
            }
            // Khớp chuẩn 15 câu (Vòng lặp chạy 10 lần)
            for (int i = 6; i <= 15; i++) {
                int idx = (i - 6) % animalImages.length;
                questions.add(createQuestion("q" + i, "Which animal is shown in the image?", "MULTIPLE_CHOICE",
                        animalOptions.get(idx), animalAnswers[idx], animalImages[idx]));
            }
        } else if ("lesson_plants".equals(lessonId)) {
            String[] plantImages = new String[] {
                    "https://images.unsplash.com/photo-1464207687429-7505649dae38?w=400",
                    "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400",
                    "https://images.unsplash.com/photo-1469484097594-6f4ee5583fb4?w=400",
                    "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=400",
                    "https://images.unsplash.com/photo-1545241749-1ceff0df5c75?w=400"
            };
            String[] plantAnswers = new String[] { "Plant", "Trees", "Leaves", "Flowers", "Cactus" };
            List<List<String>> plantOptions = Arrays.asList(
                    Arrays.asList("Plant", "Animal", "Car"),
                    Arrays.asList("Trees", "Ocean", "City"),
                    Arrays.asList("Leaves", "Stones", "Clouds"),
                    Arrays.asList("Flowers", "Fish", "Cars"),
                    Arrays.asList("Cactus", "Banana", "Snow")
            );
            for (int i = 0; i < plantImages.length; i++) {
                questions.add(createQuestion("q" + (i + 1), "What is shown in the image?", "MULTIPLE_CHOICE",
                        plantOptions.get(i), plantAnswers[i], plantImages[i]));
            }
            // Khớp chuẩn 15 câu
            for (int i = 6; i <= 15; i++) {
                int idx = (i - 6) % plantImages.length;
                questions.add(createQuestion("q" + i, "What is shown in the image?", "MULTIPLE_CHOICE",
                        plantOptions.get(idx), plantAnswers[idx], plantImages[idx]));
            }
        } else if ("lesson_fruits".equals(lessonId)) {
            String[] fruitImages = new String[] {
                    "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400",
                    "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=400",
                    "https://images.unsplash.com/photo-1587393855258-915a805b33e6?w=400",
                    "https://images.unsplash.com/photo-1585518419759-84b902c41ee4?w=400",
                    "https://images.unsplash.com/photo-1585076372410-e9dc5b26fbb2?w=400"
            };
            String[] fruitAnswers = new String[] { "Banana", "Orange", "Strawberry", "Kiwi", "Mango" };
            List<List<String>> fruitOptions = Arrays.asList(
                    Arrays.asList("Banana", "Apple", "Orange"),
                    Arrays.asList("Orange", "Lemon", "Grape"),
                    Arrays.asList("Strawberry", "Cherry", "Blueberry"),
                    Arrays.asList("Kiwi", "Pear", "Coconut"),
                    Arrays.asList("Mango", "Pineapple", "Papaya")
            );
            for (int i = 0; i < fruitImages.length; i++) {
                questions.add(createQuestion("q" + (i + 1), "Which fruit is shown in the image?", "MULTIPLE_CHOICE",
                        fruitOptions.get(i), fruitAnswers[i], fruitImages[i]));
            }
            // Khớp chuẩn 15 câu
            for (int i = 6; i <= 15; i++) {
                int idx = (i - 6) % fruitImages.length;
                questions.add(createQuestion("q" + i, "Which fruit is shown in the image?", "MULTIPLE_CHOICE",
                        fruitOptions.get(idx), fruitAnswers[idx], fruitImages[idx]));
            }
        } else if ("lesson_vegetables".equals(lessonId)) {
            String[] vegetableImages = new String[] {
                    "https://images.unsplash.com/photo-1599599810639-a6d6a8ad8e3b?w=400",
                    "https://images.unsplash.com/photo-1590969814534-aad6401665b5?w=400",
                    "https://images.unsplash.com/photo-1599599810951-f50c766c4c69?w=400",
                    "https://images.unsplash.com/photo-1599599810545-01d15e7e3d58?w=400",
                    "https://images.unsplash.com/photo-1599599810639-a6d6a8ad8e3b?w=400"
            };
            String[] vegetableAnswers = new String[] { "Carrot", "Broccoli", "Tomato", "Spinach", "Carrot" };
            List<List<String>> vegetableOptions = Arrays.asList(
                    Arrays.asList("Carrot", "Lettuce", "Cucumber"),
                    Arrays.asList("Broccoli", "Cabbage", "Onion"),
                    Arrays.asList("Tomato", "Potato", "Pepper"),
                    Arrays.asList("Spinach", "Basil", "Mint"),
                    Arrays.asList("Carrot", "Lettuce", "Cucumber")
            );
            for (int i = 0; i < vegetableImages.length; i++) {
                questions.add(createQuestion("q" + (i + 1), "Which vegetable is shown in the image?", "MULTIPLE_CHOICE",
                        vegetableOptions.get(i), vegetableAnswers[i], vegetableImages[i]));
            }
            // Khớp chuẩn 15 câu
            for (int i = 6; i <= 15; i++) {
                int idx = (i - 6) % vegetableImages.length;
                questions.add(createQuestion("q" + i, "Which vegetable is shown in the image?", "MULTIPLE_CHOICE",
                        vegetableOptions.get(idx), vegetableAnswers[idx], vegetableImages[idx]));
            }
        }

        return questions;
    }

    private Question createColorQuestion(String id, String text, List<String> options,
                                         String correctAnswer, String imageUrl, String explanation) {
        return new Question(id, "", text, "MULTIPLE_CHOICE", options, correctAnswer, 0, imageUrl, explanation);
    }

    private Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer) {
        return new Question(id, "", text, type, options, correctAnswer, 0);
    }

    private Question createQuestion(String id, String text, String type, List<String> options,
                                    String correctAnswer, String imageUrl) {
        return new Question(id, "", text, type, options, correctAnswer, 0, imageUrl);
    }

    private Question createQuestion(String id, String text, String type, List<String> options,
                                    String correctAnswer, String imageUrl, String explanation) {
        return new Question(id, "", text, type, options, correctAnswer, 0, imageUrl, explanation);
    }

    private void syncQuestionContent() {
        for (Lesson lesson : lessons) {
            String lessonId = lesson.getId();
            if (lessonId == null || lessonId.isEmpty()) {
                continue;
            }
            syncQuestionsForLesson(lessonId);
        }
    }

    private void syncQuestionsForLesson(String lessonId) {
        List<Question> templateQuestions = generateQuestions(lessonId);
        if (templateQuestions.isEmpty()) {
            return;
        }

        for (int i = 0; i < templateQuestions.size(); i++) {
            Question question = templateQuestions.get(i);
            question.setOrder(i);
            question.setLessonId(lessonId);
        }

        // Đã sửa: Cập nhật đồng bộ số lượng bài học tổng (totalQuestions) về 15 trên node tổng
        lessonsRef.child(lessonId).child("totalQuestions").setValue(15);

        lessonsRef.child(lessonId).child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // ĐOẠN ĐƯỢC CHỈNH SỬA: Tự động quét và xóa sạch những câu hỏi thừa từ index 15 đến 19 (tức là câu 16 -> 20 cũ)
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    String key = questionSnapshot.getKey();
                    if (key != null && key.matches("\\d+")) {
                        int index = Integer.parseInt(key);
                        // Nếu index từ 15 trở lên (nghĩa là từ câu hỏi thứ 16), tiến hành xóa bỏ khỏi Firebase
                        if (index >= templateQuestions.size()) {
                            questionSnapshot.getRef().removeValue();
                            continue;
                        }
                    }

                    Question template = null;
                    if (key != null && key.matches("\\d+")) {
                        int index = Integer.parseInt(key);
                        if (index >= 0 && index < templateQuestions.size()) {
                            template = templateQuestions.get(index);
                        }
                    }

                    if (template == null) {
                        Question existing = questionSnapshot.getValue(Question.class);
                        if (existing != null && existing.getId() != null) {
                            for (Question candidate : templateQuestions) {
                                if (existing.getId().equals(candidate.getId())) {
                                    template = candidate;
                                    break;
                                }
                            }
                        }
                    }

                    if (template == null) {
                        questionSnapshot.getRef().removeValue();
                        continue;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("id", template.getId());
                    updates.put("lessonId", lessonId);
                    updates.put("questionText", template.getQuestionText());
                    updates.put("questionType", template.getQuestionType());
                    updates.put("options", template.getOptions());
                    updates.put("correctAnswer", template.getCorrectAnswer());
                    updates.put("imageUrl", template.getImageUrl());
                    updates.put("explanation", template.getExplanation());
                    updates.put("order", template.getOrder());
                    questionSnapshot.getRef().updateChildren(updates);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to sync question content: " + error.getMessage());
            }
        });
    }

    private void updateAdapter() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        adapter = new LessonAdapter(lessons, requireContext());
        recyclerView.setAdapter(adapter);
    }
}