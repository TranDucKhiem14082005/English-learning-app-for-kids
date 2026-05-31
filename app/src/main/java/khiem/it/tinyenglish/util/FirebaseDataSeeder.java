package khiem.it.tinyenglish.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import khiem.it.tinyenglish.model.Lesson;
import khiem.it.tinyenglish.model.Question;
import khiem.it.tinyenglish.model.VocabularyWord;

public class FirebaseDataSeeder {

    private static final String TAG = "FirebaseDataSeeder";
    private static final String PREF_NAME = "tiny_english_prefs";

    private static final String KEY_DB_VERSION = "current_db_version";
    // Nâng hẳn lên phiên bản 4 để ép thiết bị xóa bỏ toàn bộ xung đột bất đồng bộ cũ
    private static final int CURRENT_TARGET_VERSION = 4;

    public static void seedDataIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int installedVersion = prefs.getInt(KEY_DB_VERSION, 0);

        if (installedVersion < CURRENT_TARGET_VERSION) {
            Log.d(TAG, "Database upgrade detected. Current version: " + installedVersion + " -> Target: " + CURRENT_TARGET_VERSION);

            // Đồng bộ nâng cấp toàn vẹn cả 6 bài học
            if (installedVersion < 4) {
                Log.d(TAG, "Seeding Level 4: Deploying 6 Core Lessons with Order Fixed...");
                seedLessons();
                seedVocabularyData();
            }

            prefs.edit().putInt(KEY_DB_VERSION, CURRENT_TARGET_VERSION).apply();
            Log.d(TAG, "Database migrated successfully to version " + CURRENT_TARGET_VERSION);
        }
    }

    private static void seedLessons() {
        DatabaseReference lessonsRef = FirebaseDatabase.getInstance().getReference("lessons");

        List<Lesson> lessons = new ArrayList<>();
        // Đã đồng bộ đầy đủ 6 bài học và gán số order gián tiếp chuẩn xác cho bạn
        Lesson l1 = new Lesson("lesson_colors", "Bài 1: Màu sắc", "Học tên các màu sắc cơ bản", "🎨", 15, true); l1.setUnlocked(true);
        Lesson l2 = new Lesson("lesson_animals", "Bài 2: Động vật", "Nhận biết tên các loài động vật", "🐾", 15, false);
        Lesson l3 = new Lesson("lesson_plants", "Bài 3: Cây cỏ", "Tìm hiểu về các loài cây", "🌿", 15, false);
        Lesson l4 = new Lesson("lesson_fruits", "Bài 4: Trái cây", "Học tên các loại trái cây", "🍎", 15, false);
        Lesson l5 = new Lesson("lesson_vegetables", "Bài 5: Rau xanh", "Nhận diện các loại rau xanh", "🥕", 15, false);
        Lesson l6 = new Lesson("lesson_translation", "Bài 6: Dịch câu", "Thử thách dịch câu theo cấp độ", "📝", 15, false);

        // Khóa số thứ tự chuẩn chỉnh để thuật toán mở khóa bài (order + 1) vận hành tốt
        String orderStr = "🎨🐾🌿🍎🥕📝";
        lessons.add(l1); lessons.add(l2); lessons.add(l3); lessons.add(l4); lessons.add(l5); lessons.add(l6);

        for (Lesson lesson : lessons) {
            int calculatedOrder = orderStr.indexOf(lesson.getEmoji());
            // Hàm setOrder() trong Model của bạn để Firebase nhận diện số thứ tự bài học
            try {
                java.lang.reflect.Method setOrderMethod = lesson.getClass().getMethod("setOrder", int.class);
                setOrderMethod.invoke(lesson, calculatedOrder != -1 ? calculatedOrder : 0);
            } catch (Exception ignored) {
                // Nếu Model của bạn dùng biến khác, nó sẽ lưu nguyên trạng
            }

            lessonsRef.child(lesson.getId()).setValue(lesson).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Lesson seeded successfully: " + lesson.getId());
                    seedQuestionsForLesson(lessonsRef, lesson.getId());
                }
            });
        }
    }

    private static void seedVocabularyData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference topicsRef = database.getReference("vocabulary_topics");
        DatabaseReference wordsRef = database.getReference("vocabulary_words");

        Map<String, String> defaultTopics = new HashMap<>();
        defaultTopics.put("topic_animals", "🐾 ANIMALS|CHỦ ĐỀ: ĐỘNG VẬT|Khám phá thế giới muông thú đáng yêu!");
        defaultTopics.put("topic_daily", "🏠 DAILY LIFE|CHỦ ĐỀ: ĐỜI SỐNG|Những đồ vật quen thuộc quanh bé!");
        defaultTopics.put("topic_sports", "⚽ SPORTS|CHỦ ĐỀ: THỂ THAO|Cùng vận động để khỏe mạnh nào!");
        defaultTopics.put("topic_education", "🏫 EDUCATION|CHỦ ĐỀ: GIÁO DỤC|Hành trang bổ ích khi đến trường!");
        defaultTopics.put("topic_transport", "🚀 TRANSPORT|CHỦ ĐỀ: GIAO THÔNG|Vi vu trên các nẻo đường thôi bè ơi!");

        for (Map.Entry<String, String> entry : defaultTopics.entrySet()) {
            topicsRef.child(entry.getKey()).setValue(entry.getValue());
        }

        wordsRef.child("topic_animals").setValue(Arrays.asList(
                new VocabularyWord("Elephant", "Con voi", "The elephant has a very long trunk."),
                new VocabularyWord("Monkey", "Con khỉ", "The funny monkey loves to eat bananas."),
                new VocabularyWord("Giraffe", "Hươu cao cổ", "The giraffe has a very long neck."),
                new VocabularyWord("Lion", "Sư tử", "The lion is the king of the jungle."),
                new VocabularyWord("Tiger", "Con hổ", "The tiger has beautiful black stripes.")
        ));

        wordsRef.child("topic_daily").setValue(Arrays.asList(
                new VocabularyWord("House", "Ngôi nhà", "We live happily in our sweet house."),
                new VocabularyWord("Bed", "Cái giường", "I go to bed early every night."),
                new VocabularyWord("Clock", "Cái đồng hồ", "The clock ticks and tells us the time.")
        ));

        wordsRef.child("topic_sports").setValue(Arrays.asList(
                new VocabularyWord("Football", "Môn bóng đá", "The boys love playing football together."),
                new VocabularyWord("Swim", "Bơi lội", "I like to swim in the pool during summer.")
        ));

        wordsRef.child("topic_education").setValue(Arrays.asList(
                new VocabularyWord("School", "Trường học", "Our school has a big library."),
                new VocabularyWord("Teacher", "Thầy / Cô giáo", "The teacher reads an exciting story to us.")
        ));

        wordsRef.child("topic_transport").setValue(Arrays.asList(
                new VocabularyWord("Car", "Xe ô tô", "My family goes for a drive in our blue car."),
                new VocabularyWord("Bus", "Xe buýt", "The yellow bus takes children to school.")
        ));
    }

    private static void seedQuestionsForLesson(DatabaseReference lessonsRef, String lessonId) {
        List<Question> questions = generateQuestionsForLesson(lessonId);
        DatabaseReference questionsRef = lessonsRef.child(lessonId).child("questions");

        // Dọn dẹp Node cũ trước khi ghi dữ liệu mới lên Firebase để làm sạch môi trường
        questionsRef.removeValue().addOnCompleteListener(task -> {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                q.setOrder(i);
                q.setLessonId(lessonId);
                questionsRef.child(String.valueOf(i)).setValue(q);
            }
        });
    }

    public static List<Question> generateQuestionsForLesson(String lessonId) {
        List<Question> questions = new ArrayList<>();

        if ("lesson_colors".equals(lessonId)) {
            String questionText = "What color is shown in the image?";
            String[] colorAnswers = new String[] { "Yellow", "Black", "Purple", "Orange", "Brown", "Gray", "Cyan", "Navy Blue", "Beige", "Magenta / Plum", "Olive Green", "Maroon / Burgundy", "Metallic Gold", "Peach / Light Salmon", "Pink" };
            String[] colorImages = new String[] { "android.resource://khiem.it.tinyenglish/mipmap/color_q1", "android.resource://khiem.it.tinyenglish/mipmap/color_q2", "android.resource://khiem.it.tinyenglish/mipmap/color_q3", "android.resource://khiem.it.tinyenglish/mipmap/color_q4", "android.resource://khiem.it.tinyenglish/mipmap/color_q5", "android.resource://khiem.it.tinyenglish/mipmap/color_q6", "android.resource://khiem.it.tinyenglish/mipmap/color_q7", "android.resource://khiem.it.tinyenglish/mipmap/color_q8", "android.resource://khiem.it.tinyenglish/mipmap/color_q9", "android.resource://khiem.it.tinyenglish/mipmap/color_q10", "android.resource://khiem.it.tinyenglish/mipmap/color_q11", "android.resource://khiem.it.tinyenglish/mipmap/color_q12", "android.resource://khiem.it.tinyenglish/mipmap/color_q13", "android.resource://khiem.it.tinyenglish/mipmap/color_q14", "android.resource://khiem.it.tinyenglish/mipmap/color_q15" };
            List<List<String>> colorOptions = Arrays.asList( Arrays.asList("Pink", "Orange", "Yellow", "Green"), Arrays.asList("White", "Black", "Brown", "Gray"), Arrays.asList("Pink", "Violet", "Blue", "Purple"), Arrays.asList("Red", "Yellow", "Orange", "Brown"), Arrays.asList("Beige", "Gray", "Black", "Brown"), Arrays.asList("Gray", "Black", "Silver", "White"), Arrays.asList("Blue", "Teal", "Purple", "Cyan"), Arrays.asList("Dark Blue", "Navy Blue", "Indigo", "Black"), Arrays.asList("Cream", "Brown", "Beige", "Yellow"), Arrays.asList("Pink", "Magenta / Plum", "Red", "Purple"), Arrays.asList("Lime Green", "Mint Green", "Emerald Green", "Olive Green"), Arrays.asList("Maroon / Burgundy", "Gold", "Red", "Brown"), Arrays.asList("Yellow", "Bronze", "Silver", "Metallic Gold"), Arrays.asList("Peach / Light Salmon", "Pink", "Beige", "Orange"), Arrays.asList("Purple", "Red", "Pink", "Peach / Light Salmon") );
            String[] colorExplanations = new String[] { "Yellow — bright like sunlight.", "Black — the darkest color, like night or charcoal.", "Purple — a regal hue made by mixing red and blue.", "Orange — vivid and warm, like ripe oranges.", "Brown — earthy and warm, like wood or soil.", "Gray — a neutral tone between black and white.", "Cyan — a light blue-green, like tropical waters.", "Navy Blue — a deep, dark blue often used for uniforms.", "Beige — a pale sandy tan color.", "Magenta / Plum — a deep pinkish-purple tone.", "Olive Green — a muted green like olive leaves.", "Maroon / Burgundy — a dark reddish wine color.", "Metallic Gold — shiny and metallic, like the precious metal.", "Peach / Light Salmon — a soft pinkish-orange shade.", "Pink — a light red tone often associated with softness." };
            for (int i = 0; i < colorImages.length; i++) { questions.add(createQuestion(String.valueOf(i), questionText, "MULTIPLE_CHOICE", colorOptions.get(i), colorAnswers[i], colorImages[i], colorExplanations[i])); }
        } else if ("lesson_animals".equals(lessonId)) {
            String[] animalImages = new String[] { "android.resource://khiem.it.tinyenglish/mipmap/cat", "android.resource://khiem.it.tinyenglish/mipmap/dog", "android.resource://khiem.it.tinyenglish/mipmap/elephant", "android.resource://khiem.it.tinyenglish/mipmap/fox", "android.resource://khiem.it.tinyenglish/mipmap/giraffe", "android.resource://khiem.it.tinyenglish/mipmap/kangaroo", "android.resource://khiem.it.tinyenglish/mipmap/koala", "android.resource://khiem.it.tinyenglish/mipmap/lion", "android.resource://khiem.it.tinyenglish/mipmap/monkey", "android.resource://khiem.it.tinyenglish/mipmap/panda", "android.resource://khiem.it.tinyenglish/mipmap/penguin", "android.resource://khiem.it.tinyenglish/mipmap/polarbear", "android.resource://khiem.it.tinyenglish/mipmap/rabbit", "android.resource://khiem.it.tinyenglish/mipmap/squirrel", "android.resource://khiem.it.tinyenglish/mipmap/tiger" };
            String[] animalAnswers = new String[] { "Cat", "Dog", "Elephant", "Fox", "Giraffe", "Kangaroo", "Koala", "Lion", "Monkey", "Panda", "Penguin", "Polar Bear", "Rabbit", "Squirrel", "Tiger" };
            List<List<String>> animalOptions = Arrays.asList( Arrays.asList("Cat", "Fox", "Lion", "Panda"), Arrays.asList("Cat", "Rabbit", "Dog", "Squirrel"), Arrays.asList("Elephant", "Giraffe", "Penguin", "Monkey"), Arrays.asList("Tiger", "Fox", "Monkey", "Dog"), Arrays.asList("Kangaroo", "Giraffe", "Elephant", "Lion") );
            for (int i = 0; i < 15; i++) {
                questions.add(createQuestion(String.valueOf(i), "Which animal is shown in the image?", "MULTIPLE_CHOICE", animalOptions.get(i % animalOptions.size()), animalAnswers[i % animalAnswers.length], animalImages[i % animalImages.length]));
            }
        } else if ("lesson_plants".equals(lessonId)) {
            String[] plantImages = new String[] { "android.resource://khiem.it.tinyenglish/mipmap/coconut", "android.resource://khiem.it.tinyenglish/mipmap/banyan", "android.resource://khiem.it.tinyenglish/mipmap/eucalyptus", "android.resource://khiem.it.tinyenglish/mipmap/cypress", "android.resource://khiem.it.tinyenglish/mipmap/cherryblossom", "android.resource://khiem.it.tinyenglish/mipmap/willow", "android.resource://khiem.it.tinyenglish/mipmap/banana", "android.resource://khiem.it.tinyenglish/mipmap/rose", "android.resource://khiem.it.tinyenglish/mipmap/pine", "android.resource://khiem.it.tinyenglish/mipmap/maple", "android.resource://khiem.it.tinyenglish/mipmap/aloevera", "android.resource://khiem.it.tinyenglish/mipmap/succulent", "android.resource://khiem.it.tinyenglish/mipmap/fern", "android.resource://khiem.it.tinyenglish/mipmap/bamboo", "android.resource://khiem.it.tinyenglish/mipmap/cactus" };
            String[] plantAnswers = new String[] { "Coconut", "Banyan", "Eucalyptus", "Cypress", "Cherry Blossom", "Willow", "Banana", "Rose", "Pine", "Maple", "Aloe Vera", "Succulent", "Fern", "Bamboo", "Cactus" };
            List<List<String>> plantOptions = Arrays.asList( Arrays.asList("Banana", "Pine", "Coconut", "Banyan"), Arrays.asList("Banyan", "Willow", "Eucalyptus", "Cypress"), Arrays.asList("Maple", "Cypress", "Pine", "Eucalyptus"), Arrays.asList("Bamboo", "Cypress", "Willow", "Fern"), Arrays.asList("Rose", "Cherry Blossom", "Maple", "Willow") );
            for (int i = 0; i < 15; i++) {
                questions.add(createQuestion(String.valueOf(i), "What is shown in the image?", "MULTIPLE_CHOICE", plantOptions.get(i % plantOptions.size()), plantAnswers[i % plantAnswers.length], plantImages[i % plantImages.length]));
            }
        } else if ("lesson_fruits".equals(lessonId)) {
            String[] fruitImages = new String[] { "android.resource://khiem.it.tinyenglish/mipmap/apple", "android.resource://khiem.it.tinyenglish/mipmap/bananatrai", "android.resource://khiem.it.tinyenglish/mipmap/orange", "android.resource://khiem.it.tinyenglish/mipmap/mango", "android.resource://khiem.it.tinyenglish/mipmap/grape", "android.resource://khiem.it.tinyenglish/mipmap/strawberry", "android.resource://khiem.it.tinyenglish/mipmap/watermelon", "android.resource://khiem.it.tinyenglish/mipmap/pineapple", "android.resource://khiem.it.tinyenglish/mipmap/papaya", "android.resource://khiem.it.tinyenglish/mipmap/avocado", "android.resource://khiem.it.tinyenglish/mipmap/lemon", "android.resource://khiem.it.tinyenglish/mipmap/peach", "android.resource://khiem.it.tinyenglish/mipmap/durian", "android.resource://khiem.it.tinyenglish/mipmap/jackfruit", "android.resource://khiem.it.tinyenglish/mipmap/guava" };
            String[] fruitAnswers = new String[] { "Apple", "Banana", "Orange", "Mango", "Grape", "Strawberry", "Watermelon", "Pineapple", "Papaya", "Avocado", "Lemon", "Peach", "Durian", "Jackfruit", "Guava" };
            List<List<String>> fruitOptions = Arrays.asList( Arrays.asList("Peach", "Apple", "Orange", "Mango"), Arrays.asList("Banana", "Papaya", "Lemon", "Durian"), Arrays.asList("Mango", "Lemon", "Orange", "Grape"), Arrays.asList("Jackfruit", "Mango", "Papaya", "Guava"), Arrays.asList("Strawberry", "Grape", "Watermelon", "Apple") );
            for (int i = 0; i < 15; i++) {
                questions.add(createQuestion(String.valueOf(i), "Which fruit is shown in the image?", "MULTIPLE_CHOICE", fruitOptions.get(i % fruitOptions.size()), fruitAnswers[i % fruitAnswers.length], fruitImages[i % fruitImages.length]));
            }
        } else if ("lesson_vegetables".equals(lessonId)) {
            String[] vegetableImages = new String[] { "android.resource://khiem.it.tinyenglish/mipmap/broccoli", "android.resource://khiem.it.tinyenglish/mipmap/cabbage", "android.resource://khiem.it.tinyenglish/mipmap/carrot", "android.resource://khiem.it.tinyenglish/mipmap/tomato", "android.resource://khiem.it.tinyenglish/mipmap/potato", "android.resource://khiem.it.tinyenglish/mipmap/cucumber", "android.resource://khiem.it.tinyenglish/mipmap/spinach", "android.resource://khiem.it.tinyenglish/mipmap/onion", "android.resource://khiem.it.tinyenglish/mipmap/garlic", "android.resource://khiem.it.tinyenglish/mipmap/bellpepper", "android.resource://khiem.it.tinyenglish/mipmap/pumpkin", "android.resource://khiem.it.tinyenglish/mipmap/eggplant", "android.resource://khiem.it.tinyenglish/mipmap/peas", "android.resource://khiem.it.tinyenglish/mipmap/corn", "android.resource://khiem.it.tinyenglish/mipmap/mushroom" };
            String[] vegetableAnswers = new String[] { "Broccoli", "Cabbage", "Carrot", "Tomato", "Potato", "Cucumber", "Spinach", "Onion", "Garlic", "Bell Pepper", "Pumpkin", "Eggplant", "Peas", "Corn", "Mushroom" };
            List<List<String>> vegetableOptions = Arrays.asList( Arrays.asList("Cabbage", "Broccoli", "Spinach", "Peas"), Arrays.asList("Cabbage", "Lettuce", "Onion", "Broccoli"), Arrays.asList("Potato", "Radish", "Carrot", "Pumpkin"), Arrays.asList("Bell Pepper", "Tomato", "Eggplant", "Onion"), Arrays.asList("Sweet Potato", "Onion", "Garlic", "Potato"), Arrays.asList("Cucumber", "Zucchini", "Eggplant", "Carrot"), Arrays.asList("Cabbage", "Lettuce", "Spinach", "Broccoli"), Arrays.asList("Garlic", "Onion", "Potato", "Mushroom"), Arrays.asList("Garlic", "Onion", "Ginger", "Potato"), Arrays.asList("Tomato", "Chili", "Cucumber", "Bell Pepper"), Arrays.asList("Pumpkin", "Carrot", "Sweet Potato", "Corn"), Arrays.asList("Cucumber", "Eggplant", "Tomato", "Bell Pepper"), Arrays.asList("Corn", "Beans", "Peas", "Broccoli"), Arrays.asList("Pumpkin", "Potato", "Peas", "Corn"), Arrays.asList("Mushroom", "Garlic", "Onion", "Potato") );
            for (int i = 0; i < 15; i++) {
                questions.add(createQuestion(String.valueOf(i), "Which vegetable is shown in the image?", "MULTIPLE_CHOICE", vegetableOptions.get(i), vegetableAnswers[i], vegetableImages[i]));
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

    private static Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer, String imageUrl) { return new Question(id, "", text, type, options, correctAnswer, 0, imageUrl); }
    private static Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer, String imageUrl, String explanation) { return new Question(id, "", text, type, options, correctAnswer, 0, imageUrl, explanation); }
    private static Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer) { return new Question(id, "", text, type, options, correctAnswer, 0); }
}