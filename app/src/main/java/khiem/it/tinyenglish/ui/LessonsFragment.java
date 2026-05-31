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

                if (snapshot.getChildrenCount() == 0 || !snapshot.hasChild("lesson_translation")) {
                    Log.d(TAG, "No lessons found or missing Lesson 6, seeding data...");
                    seedLessons();
                } else {
                    for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                        Lesson lesson = lessonSnapshot.getValue(Lesson.class);
                        if (lesson != null) {
                            lesson.setTotalQuestions(15);
                            lessons.add(lesson);
                        }
                    }
                    lessons.sort((l1, l2) -> {
                        String order = "🎨🐾🌿🍎🥕📝";
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
        lessonMap.put("lesson_colors", new Lesson("lesson_colors", "Bài 1: Màu sắc", "Học tên các màu sắc cơ bản", "🎨", 15, true));
        lessonMap.put("lesson_animals", new Lesson("lesson_animals", "Bài 2: Động  vật", "Nhận biết tên các loài động vật", "🐾", 15, false));
        lessonMap.put("lesson_plants", new Lesson("lesson_plants", "Bài 3: Cây cỏ", "Tìm hiểu về các loài cây", "🌿", 15, false));
        lessonMap.put("lesson_fruits", new Lesson("lesson_fruits", "Bài 4: Trái cây", "Học tên các loại trái cây", "🍎", 15, false));
        lessonMap.put("lesson_vegetables", new Lesson("lesson_vegetables", "Bài 5: Rau xanh", "Nhận diện các loại rau xanh", "🥕", 15, false));
        lessonMap.put("lesson_translation", new Lesson("lesson_translation", "Bài 6: Dịch câu", "Thử thách dịch câu theo cấp độ", "📝", 15, false));

        for (Map.Entry<String, Lesson> entry : lessonMap.entrySet()) {
            String lessonId = entry.getKey();
            Lesson lesson = entry.getValue();

            // MẸO CHỐNG LỖI LAMBDA HOÀN HẢO:
            // Tạo một biến sao chép cục bộ biệt lập hoàn toàn và khóa cứng nó bằng 'final'
            final String safeLessonId = lessonId;

            lessonsRef.child(safeLessonId).setValue(lesson).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Lesson seeded: " + safeLessonId);

                    // ĐÃ SỬA: Dùng biến safeLessonId đã được khóa cứng, Java sẽ không bao giờ gạch đỏ nữa!
                    seedQuestionsForLesson(safeLessonId);
                } else {
                    Log.e(TAG, "Failed to seed lesson: " + safeLessonId);
                }
            });
        }

        lessons.clear();
        lessons.addAll(lessonMap.values());
        lessons.sort((l1, l2) -> {
            String order = "🎨🐾🌿🍎🥕📝";
            int idx1 = order.indexOf(l1.getEmoji());
            int idx2 = order.indexOf(l2.getEmoji());
            return Integer.compare(idx1, idx2);
        });
        updateAdapter();
    }

    private void seedQuestionsForLesson(String lessonId) {
        DatabaseReference questionsRef = lessonsRef.child(lessonId).child("questions");
        List<Question> questions = generateQuestions(lessonId);

        questionsRef.removeValue().addOnCompleteListener(task -> {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                q.setOrder(i);
                q.setLessonId(lessonId);
                questionsRef.child(String.valueOf(i)).setValue(q);
            }
        });
    }

    private List<Question> generateQuestions(String lessonId) {
        List<Question> questions = new ArrayList<>();

        if ("lesson_colors".equals(lessonId)) {
            String questionText = "What color is shown in the image?";
            String[] colorAnswers = new String[] {
                    "Yellow", "Black", "Purple", "Orange", "Brown", "Gray", "Cyan", "Navy Blue", "Beige", "Magenta / Plum", "Olive Green", "Maroon / Burgundy", "Metallic Gold", "Peach / Light Salmon", "Pink"
            };
            String[] colorImages = new String[] {
                    "android.resource://khiem.it.tinyenglish/mipmap/color_q1", "android.resource://khiem.it.tinyenglish/mipmap/color_q2", "android.resource://khiem.it.tinyenglish/mipmap/color_q3", "android.resource://khiem.it.tinyenglish/mipmap/color_q4", "android.resource://khiem.it.tinyenglish/mipmap/color_q5", "android.resource://khiem.it.tinyenglish/mipmap/color_q6", "android.resource://khiem.it.tinyenglish/mipmap/color_q7", "android.resource://khiem.it.tinyenglish/mipmap/color_q8", "android.resource://khiem.it.tinyenglish/mipmap/color_q9", "android.resource://khiem.it.tinyenglish/mipmap/color_q10", "android.resource://khiem.it.tinyenglish/mipmap/color_q11", "android.resource://khiem.it.tinyenglish/mipmap/color_q12", "android.resource://khiem.it.tinyenglish/mipmap/color_q13", "android.resource://khiem.it.tinyenglish/mipmap/color_q14", "android.resource://khiem.it.tinyenglish/mipmap/color_q15"
            };
            List<List<String>> colorOptions = Arrays.asList(
                    Arrays.asList("Pink", "Orange", "Yellow", "Green"), Arrays.asList("White", "Black", "Brown", "Gray"), Arrays.asList("Pink", "Violet", "Blue", "Purple"), Arrays.asList("Red", "Yellow", "Orange", "Brown"), Arrays.asList("Beige", "Gray", "Black", "Brown"), Arrays.asList("Gray", "Black", "Silver", "White"), Arrays.asList("Blue", "Teal", "Purple", "Cyan"), Arrays.asList("Dark Blue", "Navy Blue", "Indigo", "Black"), Arrays.asList("Cream", "Brown", "Beige", "Yellow"), Arrays.asList("Pink", "Magenta / Plum", "Red", "Purple"), Arrays.asList("Lime Green", "Mint Green", "Emerald Green", "Olive Green"), Arrays.asList("Maroon / Burgundy", "Gold", "Red", "Brown"), Arrays.asList("Yellow", "Bronze", "Silver", "Metallic Gold"), Arrays.asList("Peach / Light Salmon", "Pink", "Beige", "Orange"), Arrays.asList("Purple", "Red", "Pink", "Peach / Light Salmon")
            );
            String[] colorExplanations = new String[] {
                    "Yellow — bright like sunlight.", "Black — the darkest color, like night or charcoal.", "Purple — a regal hue made by mixing red and blue.", "Orange — vivid and warm, like ripe oranges.", "Brown — earthy and warm, like wood or soil.", "Gray — a neutral tone between black and white.", "Cyan — a light blue-green, like tropical waters.", "Navy Blue — a deep, dark blue often used for uniforms.", "Beige — a pale sandy tan color.", "Magenta / Plum — a deep pinkish-purple tone.", "Olive Green — a muted green like olive leaves.", "Maroon / Burgundy — a dark reddish wine color.", "Metallic Gold — shiny and metallic, like the precious metal.", "Peach / Light Salmon — a soft pinkish-orange shade.", "Pink — a light red tone often associated with softness."
            };
            for (int i = 0; i < colorImages.length; i++) {
                questions.add(createColorQuestion(String.valueOf(i), questionText, colorOptions.get(i), colorAnswers[i], colorImages[i], colorExplanations[i]));
            }
        } else if ("lesson_animals".equals(lessonId)) {
            String[] animalImages = new String[] {
                    "android.resource://khiem.it.tinyenglish/mipmap/cat", "android.resource://khiem.it.tinyenglish/mipmap/dog", "android.resource://khiem.it.tinyenglish/mipmap/elephant", "android.resource://khiem.it.tinyenglish/mipmap/fox", "android.resource://khiem.it.tinyenglish/mipmap/giraffe", "android.resource://khiem.it.tinyenglish/mipmap/kangaroo", "android.resource://khiem.it.tinyenglish/mipmap/koala", "android.resource://khiem.it.tinyenglish/mipmap/lion", "android.resource://khiem.it.tinyenglish/mipmap/monkey", "android.resource://khiem.it.tinyenglish/mipmap/panda", "android.resource://khiem.it.tinyenglish/mipmap/penguin", "android.resource://khiem.it.tinyenglish/mipmap/polarbear", "android.resource://khiem.it.tinyenglish/mipmap/rabbit", "android.resource://khiem.it.tinyenglish/mipmap/squirrel", "android.resource://khiem.it.tinyenglish/mipmap/tiger"
            };
            String[] animalAnswers = new String[] { "Cat", "Dog", "Elephant", "Fox", "Giraffe", "Kangaroo", "Koala", "Lion", "Monkey", "Panda", "Penguin", "Polar Bear", "Rabbit", "Squirrel", "Tiger" };
            List<List<String>> animalOptions = Arrays.asList(
                    Arrays.asList("Cat", "Fox", "Lion", "Panda"), Arrays.asList("Cat", "Rabbit", "Dog", "Squirrel"), Arrays.asList("Elephant", "Giraffe", "Penguin", "Monkey"), Arrays.asList("Tiger", "Fox", "Monkey", "Dog"), Arrays.asList("Kangaroo", "Giraffe", "Elephant", "Lion")
            );
            // ĐÃ SỬA: Tách biệt chỉ số an toàn độc lập theo độ dài của từng mảng chữ, mảng hình riêng biệt
            for (int i = 0; i < 15; i++) {
                int imgIdx = i % animalImages.length;
                int optIdx = i % animalOptions.size();
                int ansIdx = i % animalAnswers.length;
                questions.add(createQuestion(String.valueOf(i), "Which animal is shown in the image?", "MULTIPLE_CHOICE", animalOptions.get(optIdx), animalAnswers[ansIdx], animalImages[imgIdx]));
            }
        } else if ("lesson_plants".equals(lessonId)) {
            String[] plantImages = new String[] {
                    "android.resource://khiem.it.tinyenglish/mipmap/coconut", "android.resource://khiem.it.tinyenglish/mipmap/banyan", "android.resource://khiem.it.tinyenglish/mipmap/eucalyptus", "android.resource://khiem.it.tinyenglish/mipmap/cypress", "android.resource://khiem.it.tinyenglish/mipmap/cherryblossom", "android.resource://khiem.it.tinyenglish/mipmap/willow", "android.resource://khiem.it.tinyenglish/mipmap/banana", "android.resource://khiem.it.tinyenglish/mipmap/rose", "android.resource://khiem.it.tinyenglish/mipmap/pine", "android.resource://khiem.it.tinyenglish/mipmap/maple", "android.resource://khiem.it.tinyenglish/mipmap/aloevera", "android.resource://khiem.it.tinyenglish/mipmap/succulent", "android.resource://khiem.it.tinyenglish/mipmap/fern", "android.resource://khiem.it.tinyenglish/mipmap/bamboo", "android.resource://khiem.it.tinyenglish/mipmap/cactus"
            };
            String[] plantAnswers = new String[] { "Coconut", "Banyan", "Eucalyptus", "Cypress", "Cherry Blossom", "Willow", "Banana", "Rose", "Pine", "Maple", "Aloe Vera", "Succulent", "Fern", "Bamboo", "Cactus" };
            List<List<String>> plantOptions = Arrays.asList(
                    Arrays.asList("Banana", "Pine", "Coconut", "Banyan"), Arrays.asList("Banyan", "Willow", "Eucalyptus", "Cypress"), Arrays.asList("Maple", "Cypress", "Pine", "Eucalyptus"), Arrays.asList("Bamboo", "Cypress", "Willow", "Fern"), Arrays.asList("Rose", "Cherry Blossom", "Maple", "Willow")
            );
            // ĐÃ SỬA: Bảo vệ an toàn chống tràn mảng cho Bài Cây Cỏ
            for (int i = 0; i < 15; i++) {
                int imgIdx = i % plantImages.length;
                int optIdx = i % plantOptions.size();
                int ansIdx = i % plantAnswers.length;
                questions.add(createQuestion(String.valueOf(i), "What is shown in the image?", "MULTIPLE_CHOICE", plantOptions.get(optIdx), plantAnswers[ansIdx], plantImages[imgIdx]));
            }
        } else if ("lesson_fruits".equals(lessonId)) {
            String[] fruitImages = new String[] {
                    "android.resource://khiem.it.tinyenglish/mipmap/apple", "android.resource://khiem.it.tinyenglish/mipmap/bananatrai", "android.resource://khiem.it.tinyenglish/mipmap/orange", "android.resource://khiem.it.tinyenglish/mipmap/mango", "android.resource://khiem.it.tinyenglish/mipmap/grape", "android.resource://khiem.it.tinyenglish/mipmap/strawberry", "android.resource://khiem.it.tinyenglish/mipmap/watermelon", "android.resource://khiem.it.tinyenglish/mipmap/pineapple", "android.resource://khiem.it.tinyenglish/mipmap/papaya", "android.resource://khiem.it.tinyenglish/mipmap/avocado", "android.resource://khiem.it.tinyenglish/mipmap/lemon", "android.resource://khiem.it.tinyenglish/mipmap/peach", "android.resource://khiem.it.tinyenglish/mipmap/durian", "android.resource://khiem.it.tinyenglish/mipmap/jackfruit", "android.resource://khiem.it.tinyenglish/mipmap/guava"
            };
            String[] fruitAnswers = new String[] { "Apple", "Banana", "Orange", "Mango", "Grape", "Strawberry", "Watermelon", "Pineapple", "Papaya", "Avocado", "Lemon", "Peach", "Durian", "Jackfruit", "Guava" };
            List<List<String>> fruitOptions = Arrays.asList(
                    Arrays.asList("Peach", "Apple", "Orange", "Mango"), Arrays.asList("Banana", "Papaya", "Lemon", "Durian"), Arrays.asList("Mango", "Lemon", "Orange", "Grape"), Arrays.asList("Jackfruit", "Mango", "Papaya", "Guava"), Arrays.asList("Strawberry", "Grape", "Watermelon", "Apple")
            );
            // ĐÃ SỬA: Bảo vệ an toàn chống tràn mảng cho Bài Trái Cây
            for (int i = 0; i < 15; i++) {
                int imgIdx = i % fruitImages.length;
                int optIdx = i % fruitOptions.size();
                int ansIdx = i % fruitAnswers.length;
                questions.add(createQuestion(String.valueOf(i), "Which fruit is shown in the image?", "MULTIPLE_CHOICE", fruitOptions.get(optIdx), fruitAnswers[ansIdx], fruitImages[imgIdx]));
            }
        } else if ("lesson_vegetables".equals(lessonId)) {
            String[] vegetableImages = new String[] {
                    "android.resource://khiem.it.tinyenglish/mipmap/broccoli",
                    "android.resource://khiem.it.tinyenglish/mipmap/cabbage",
                    "android.resource://khiem.it.tinyenglish/mipmap/carrot",
                    "android.resource://khiem.it.tinyenglish/mipmap/tomato",
                    "android.resource://khiem.it.tinyenglish/mipmap/potato",
                    "android.resource://khiem.it.tinyenglish/mipmap/cucumber",
                    "android.resource://khiem.it.tinyenglish/mipmap/spinach",
                    "android.resource://khiem.it.tinyenglish/mipmap/onion",
                    "android.resource://khiem.it.tinyenglish/mipmap/garlic",
                    "android.resource://khiem.it.tinyenglish/mipmap/bellpepper",
                    "android.resource://khiem.it.tinyenglish/mipmap/pumpkin",
                    "android.resource://khiem.it.tinyenglish/mipmap/eggplant",
                    "android.resource://khiem.it.tinyenglish/mipmap/peas",
                    "android.resource://khiem.it.tinyenglish/mipmap/corn",
                    "android.resource://khiem.it.tinyenglish/mipmap/mushroom"
            };
            String[] vegetableAnswers = new String[] { "Broccoli", "Cabbage", "Carrot", "Tomato", "Potato", "Cucumber", "Spinach", "Onion", "Garlic", "Bell Pepper", "Pumpkin", "Eggplant", "Peas", "Corn", "Mushroom"};
            List<List<String>> vegetableOptions = Arrays.asList(
// 1. Broccoli (Bông cải xanh)
                    Arrays.asList("Cabbage", "Broccoli", "Spinach", "Peas"),
                    // 2. Cabbage (Bắp cải)
                    Arrays.asList("Cabbage", "Lettuce", "Onion", "Broccoli"),
                    // 3. Carrot (Củ cà rốt)
                    Arrays.asList("Potato", "Radish", "Carrot", "Pumpkin"),
                    // 4. Tomato (Cà chua)
                    Arrays.asList("Bell Pepper", "Tomato", "Eggplant", "Onion"),
                    // 5. Potato (Khoai tây)
                    Arrays.asList("Sweet Potato", "Onion", "Garlic", "Potato"),
                    // 6. Cucumber (Dưa leo)
                    Arrays.asList("Cucumber", "Zucchini", "Eggplant", "Carrot"),
                    // 7. Spinach (Rau chân vịt / Cải bó xôi)
                    Arrays.asList("Cabbage", "Lettuce", "Spinach", "Broccoli"),
                    // 8. Onion (Hành tây)
                    Arrays.asList("Garlic", "Onion", "Potato", "Mushroom"),
                    // 9. Garlic (Tỏi)
                    Arrays.asList("Garlic", "Onion", "Ginger", "Potato"),
                    // 10. Bell Pepper (Ớt chuông)
                    Arrays.asList("Tomato", "Chili", "Cucumber", "Bell Pepper"),
                    // 11. Pumpkin (Bí đỏ)
                    Arrays.asList("Pumpkin", "Carrot", "Sweet Potato", "Corn"),
                    // 12. Eggplant (Cà tím)
                    Arrays.asList("Cucumber", "Eggplant", "Tomato", "Bell Pepper"),
                    // 13. Peas (Đậu Hà Lan)
                    Arrays.asList("Corn", "Beans", "Peas", "Broccoli"),
                    // 14. Corn (Bắp / Ngô)
                    Arrays.asList("Pumpkin", "Potato", "Peas", "Corn"),
                    // 15. Mushroom (Nấm)
                    Arrays.asList("Mushroom", "Garlic", "Onion", "Potato")            );
            // ĐÃ SỬA: Bảo vệ an toàn chống tràn mảng cho Bài Rau Xanh
            for (int i = 0; i < 15; i++) {
                int imgIdx = i % vegetableImages.length;
                int optIdx = i % vegetableOptions.size();
                int ansIdx = i % vegetableAnswers.length;
                questions.add(createQuestion(String.valueOf(i), "Which vegetable is shown in the image?", "MULTIPLE_CHOICE", vegetableOptions.get(optIdx), vegetableAnswers[ansIdx], vegetableImages[imgIdx]));
            }
        } else if ("lesson_translation".equals(lessonId)) {
            questions.add(createQuestion("0", "Bạn muốn dùng cà phê hay trà?|Would you like coffee or ...?", "GUIDED_TRANSLATION", Arrays.asList("Tea", "Milk", "Orange juice", "Apple juice"), "Tea"));
            questions.add(createQuestion("1", "Tôi có một chú chó nhỏ màu nâu.|I have a small brown ....", "GUIDED_TRANSLATION", Arrays.asList("Cat", "Dog", "Rabbit", "Bird"), "Dog"));
            questions.add(createQuestion("2", "Bông hoa này thật đẹp.|This ... is very beautiful.", "GUIDED_TRANSLATION", Arrays.asList("Tree", "Leaf", "Flower", "Grass"), "Flower"));
            questions.add(createQuestion("3", "Bầu trời hôm nay có màu xanh dương.|The sky is ... today.", "GUIDED_TRANSLATION", Arrays.asList("Red", "Yellow", "Green", "Blue"), "Blue"));
            questions.add(createQuestion("4", "Quả chuối này rất ngọt.|This yellow ... is very sweet.", "GUIDED_TRANSLATION", Arrays.asList("Apple", "Banana", "Mango", "Orange"), "Banana"));
            questions.add(createQuestion("5", "Chúng tôi chạy bộ trong công viên xanh mát.|We run fast around the green ....", "GUIDED_TRANSLATION", Arrays.asList("Classroom", "Forest", "Park", "Garden"), "Park"));
            questions.add(createQuestion("6", "Cô giáo đang đọc một câu chuyện thú vị.|The teacher is reading an exciting ....", "GUIDED_TRANSLATION", Arrays.asList("Lesson", "Story", "Notebook", "Letter"), "Story"));
            questions.add(createQuestion("7", "Nhớ đánh răng mỗi buổi sáng nhé.|Remember to ... your teeth every morning.", "GUIDED_TRANSLATION", Arrays.asList("Brush", "Wash", "Clean", "Rub"), "Brush"));
            questions.add(createQuestion("8", "Tôi làm bài tập về nhà vào quyển vở.|I write my homework in the ....", "GUIDED_TRANSLATION", Arrays.asList("Ruler", "Eraser", "Notebook", "Pencil"), "Notebook"));
            questions.add(createQuestion("9", "Chú khỉ thích leo trèo lên cây chuối.|The monkey loves to ... the banana tree.", "GUIDED_TRANSLATION", Arrays.asList("Swim", "Run", "Jump", "Climb"), "Climb"));
            questions.add(createQuestion("10", "Bạn có muốn đi xem phim tối nay không?|Would you like to ... tonight?", "GUIDED_TRANSLATION", Arrays.asList("go to the cinema", "watch TV at home", "play video games", "sleep early"), "go to the cinema"));
            questions.add(createQuestion("11", "Chú sóc nhỏ đang giấu thức ăn dưới cành cây.|The little squirrel is ... under the tree.", "GUIDED_TRANSLATION", Arrays.asList("eating honey", "chasing a mouse", "hiding its food", "flying high"), "hiding its food"));
            questions.add(createQuestion("12", "Chiếc máy bay khổng lồ đang bay cao trên bầu trời.|The giant plane is ... in the blue sky.", "GUIDED_TRANSLATION", Arrays.asList("swimming fast", "flying high", "sailing slowly", "running fast"), "flying high"));
            questions.add(createQuestion("13", "Chú chim cánh cụt đi bộ chậm rãi trên tuyết trắng.|The penguin ... on the white snow.", "GUIDED_TRANSLATION", Arrays.asList("swims in the pool", "flies in the sky", "walks slowly", "climbs the tree"), "walks slowly"));
            questions.add(createQuestion("14", "Quả dưa hấu thì to, xanh và mọng nước.|Watermelon is ... and juicy.", "GUIDED_TRANSLATION", Arrays.asList("small and sour", "big, green", "long and yellow", "soft and brown"), "big, green"));
        }

        return questions;
    }

    private Question createColorQuestion(String id, String text, List<String> options, String correctAnswer, String imageUrl, String explanation) {
        return new Question(id, "", text, "MULTIPLE_CHOICE", options, correctAnswer, 0, imageUrl, explanation);
    }

    private Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer) {
        return new Question(id, "", text, type, options, correctAnswer, 0);
    }

    private Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer, String imageUrl) {
        return new Question(id, "", text, type, options, correctAnswer, 0, imageUrl);
    }

    private Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer, String imageUrl, String explanation) {
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

        lessonsRef.child(lessonId).child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    String key = questionSnapshot.getKey();

                    if (key == null || !key.matches("\\d+") || key.startsWith("q") || key.startsWith("t")) {
                        questionSnapshot.getRef().removeValue();
                        continue;
                    }

                    int index = Integer.parseInt(key);
                    if (index >= templateQuestions.size()) {
                        questionSnapshot.getRef().removeValue();
                        continue;
                    }

                    Question template = templateQuestions.get(index);
                    if (template != null) {
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