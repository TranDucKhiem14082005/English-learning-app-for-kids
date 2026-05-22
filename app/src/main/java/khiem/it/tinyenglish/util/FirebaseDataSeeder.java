package khiem.it.tinyenglish.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import khiem.it.tinyenglish.model.Lesson;
import khiem.it.tinyenglish.model.Question;

public class FirebaseDataSeeder {

    private static final String TAG = "FirebaseDataSeeder";
    private static final String PREF_NAME = "tiny_english_prefs";
    private static final String KEY_LESSONS_SEEDED = "lessons_seeded";

    public static void seedDataIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean seeded = prefs.getBoolean(KEY_LESSONS_SEEDED, false);

        if (!seeded) {
            Log.d(TAG, "Seeding Firebase data...");
            seedLessons(context);
        }
    }

    private static void seedLessons(Context context) {
        DatabaseReference lessonsRef = FirebaseDatabase.getInstance().getReference("lessons");

        List<Lesson> lessons = new ArrayList<>();
        lessons.add(new Lesson("lesson_colors", "Colors", "Learn basic color names", "🎨", 15, true));
        lessons.add(new Lesson("lesson_animals", "Animals", "Identify animal names", "🐾", 20, false));
        lessons.add(new Lesson("lesson_plants", "Plants", "Discover plant types", "🌿", 20, false));
        lessons.add(new Lesson("lesson_fruits", "Fruits", "Learn fruit names", "🍎", 20, false));
        lessons.add(new Lesson("lesson_vegetables", "Vegetables", "Identify vegetable types", "🥕", 20, false));

        for (Lesson lesson : lessons) {
            lessonsRef.child(lesson.getId()).setValue(lesson).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Lesson seeded: " + lesson.getId());
                    // Seed questions for this lesson
                    seedQuestionsForLesson(lessonsRef, lesson.getId());
                } else {
                    Log.e(TAG, "Failed to seed lesson: " + lesson.getId());
                }
            });
        }

        // Mark as seeded
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LESSONS_SEEDED, true).apply();
    }

    private static void seedQuestionsForLesson(DatabaseReference lessonsRef, String lessonId) {
        List<Question> questions = generateQuestionsForLesson(lessonId);
        DatabaseReference questionsRef = lessonsRef.child(lessonId).child("questions");
        
        for (Question question : questions) {
            questionsRef.child(question.getId()).setValue(question);
        }
    }

    public static List<Question> generateQuestionsForLesson(String lessonId) {
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
                    "Yellow — bright like sunlight.",
                    "Black — the darkest color, like night or charcoal.",
                    "Purple — a regal hue made by mixing red and blue.",
                    "Orange — vivid and warm, like ripe oranges.",
                    "Brown — earthy and warm, like wood or soil.",
                    "Gray — a neutral tone between black and white.",
                    "Cyan — a light blue-green, like tropical waters.",
                    "Navy Blue — a deep, dark blue often used for uniforms.",
                    "Beige — a pale sandy tan color.",
                    "Magenta / Plum — a deep pinkish-purple tone.",
                    "Olive Green — a muted green like olive leaves.",
                    "Maroon / Burgundy — a dark reddish wine color.",
                    "Metallic Gold — shiny and metallic, like the precious metal.",
                    "Peach / Light Salmon — a soft pinkish-orange shade.",
                    "Pink — a light red tone often associated with softness."
            };

            for (int i = 0; i < colorImages.length; i++) {
                questions.add(createQuestion("q" + (i + 1), questionText, "MULTIPLE_CHOICE",
                        colorOptions.get(i), colorAnswers[i], i + 1,
                        colorImages[i], colorExplanations[i]));
            }
        } else if ("lesson_animals".equals(lessonId)) {
            questions.add(createQuestion("q1", "What sound does a dog make?", "MULTIPLE_CHOICE",
                    Arrays.asList("Bark", "Meow", "Roar"), "Bark", 1,
                    "https://images.unsplash.com/photo-1633722715463-d30628519f0f?w=400"));
            questions.add(createQuestion("q2", "What is the king of the jungle?", "MULTIPLE_CHOICE",
                    Arrays.asList("Tiger", "Lion", "Elephant"), "Lion", 2,
                    "https://images.unsplash.com/photo-1614613535308-eb5fbd8a2c17?w=400"));
            questions.add(createQuestion("q3", "A cat can fly", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "False", 3,
                    "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400"));
            questions.add(createQuestion("q4", "What animal says 'moo'?", "MULTIPLE_CHOICE",
                    Arrays.asList("Pig", "Cow", "Duck"), "Cow", 4,
                    "https://images.unsplash.com/photo-1564349688535-5866d19002ff?w=400"));
            questions.add(createQuestion("q5", "Birds can lay eggs", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 5,
                    "https://images.unsplash.com/photo-1444464666175-1642a9c67b88?w=400"));
            
            for (int i = 6; i <= 20; i++) {
                String animal = i % 3 == 0 ? "Dog" : (i % 3 == 1 ? "Cat" : "Bird");
                String question_text = "What animal is number " + i + "?";
                questions.add(createQuestion("q" + i, question_text, "MULTIPLE_CHOICE",
                        Arrays.asList("Dog", "Cat", "Bird"), animal, i, ""));
            }
        } else if ("lesson_plants".equals(lessonId)) {
            questions.add(createQuestion("q1", "What do plants need to grow?", "MULTIPLE_CHOICE",
                    Arrays.asList("Water and sunlight", "Only water", "Only air"), "Water and sunlight", 1,
                    "https://images.unsplash.com/photo-1464207687429-7505649dae38?w=400"));
            questions.add(createQuestion("q2", "Plants are living things", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 2,
                    "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400"));
            questions.add(createQuestion("q3", "What color are most leaves?", "MULTIPLE_CHOICE",
                    Arrays.asList("Green", "Brown", "Yellow"), "Green", 3,
                    "https://images.unsplash.com/photo-1469484097594-6f4ee5583fb4?w=400"));
            questions.add(createQuestion("q4", "Flowers help plants to reproduce", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 4,
                    "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=400"));
            questions.add(createQuestion("q5", "Cactus needs a lot of water", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "False", 5,
                    "https://images.unsplash.com/photo-1545241749-1ceff0df5c75?w=400"));
            
            for (int i = 6; i <= 20; i++) {
                String question_text = "Plant fact number " + i + "?";
                questions.add(createQuestion("q" + i, question_text, "MULTIPLE_CHOICE",
                        Arrays.asList("A", "B", "C"), "A", i, ""));
            }
        } else if ("lesson_fruits".equals(lessonId)) {
            questions.add(createQuestion("q1", "Which fruit is yellow and curved?", "MULTIPLE_CHOICE",
                    Arrays.asList("Banana", "Apple", "Orange"), "Banana", 1,
                    "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400"));
            questions.add(createQuestion("q2", "What vitamin does orange have?", "MULTIPLE_CHOICE",
                    Arrays.asList("Vitamin A", "Vitamin C", "Vitamin D"), "Vitamin C", 2,
                    "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=400"));
            questions.add(createQuestion("q3", "Strawberry is a type of fruit", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 3,
                    "https://images.unsplash.com/photo-1587393855258-915a805b33e6?w=400"));
            questions.add(createQuestion("q4", "Which fruit is green on the outside and white inside?", "MULTIPLE_CHOICE",
                    Arrays.asList("Kiwi", "Lime", "Coconut"), "Kiwi", 4,
                    "https://images.unsplash.com/photo-1585518419759-84b902c41ee4?w=400"));
            questions.add(createQuestion("q5", "Mango is the king of fruits", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 5,
                    "https://images.unsplash.com/photo-1585076372410-e9dc5b26fbb2?w=400"));
            
            for (int i = 6; i <= 20; i++) {
                String fruit = i % 3 == 0 ? "Apple" : (i % 3 == 1 ? "Mango" : "Banana");
                String question_text = "Fruit number " + i + "?";
                questions.add(createQuestion("q" + i, question_text, "MULTIPLE_CHOICE",
                        Arrays.asList("Apple", "Mango", "Banana"), fruit, i, ""));
            }
        } else if ("lesson_vegetables".equals(lessonId)) {
            questions.add(createQuestion("q1", "What vegetable is orange colored?", "MULTIPLE_CHOICE",
                    Arrays.asList("Carrot", "Lettuce", "Cucumber"), "Carrot", 1,
                    "https://images.unsplash.com/photo-1599599810639-a6d6a8ad8e3b?w=400"));
            questions.add(createQuestion("q2", "Broccoli looks like a small tree", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 2,
                    "https://images.unsplash.com/photo-1590969814534-aad6401665b5?w=400"));
            questions.add(createQuestion("q3", "What vegetable is round and red?", "MULTIPLE_CHOICE",
                    Arrays.asList("Tomato", "Potato", "Pepper"), "Tomato", 3,
                    "https://images.unsplash.com/photo-1599599810951-f50c766c4c69?w=400"));
            questions.add(createQuestion("q4", "Potatoes grow underground", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 4,
                    "https://images.unsplash.com/photo-1585518419759-84b902c41ee4?w=400"));
            questions.add(createQuestion("q5", "Spinach is high in iron", "TRUE_FALSE",
                    Arrays.asList("True", "False"), "True", 5,
                    "https://images.unsplash.com/photo-1599599810545-01d15e7e3d58?w=400"));
            
            for (int i = 6; i <= 20; i++) {
                String veg = i % 3 == 0 ? "Carrot" : (i % 3 == 1 ? "Tomato" : "Lettuce");
                String question_text = "Vegetable number " + i + "?";
                questions.add(createQuestion("q" + i, question_text, "MULTIPLE_CHOICE",
                        Arrays.asList("Carrot", "Tomato", "Lettuce"), veg, i, ""));
            }
        }

        return questions;
    }

    private static Question createQuestion(String id, String text, String type, List<String> options, String correctAnswer, int order, String imageUrl) {
        return new Question(id, "", text, type, options, correctAnswer, order, imageUrl);
    }

        private static Question createQuestion(String id, String text, String type, List<String> options,
                                                                                  String correctAnswer, int order, String imageUrl, String explanation) {
                return new Question(id, "", text, type, options, correctAnswer, order, imageUrl, explanation);
        }
}
