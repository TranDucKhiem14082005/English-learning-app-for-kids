package khiem.it.tinyenglish.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.adapter.VocabularyAdapter;
import khiem.it.tinyenglish.model.VocabularyWord;

public class VocabularyFragment extends Fragment {

    private ExpandableListView expandableListView;
    private FloatingActionButton fabAddVocabulary;
    private VocabularyAdapter adapter;
    private List<String> listTopic;
    private Map<String, List<VocabularyWord>> listWordsDetail;

    // Các chuỗi hằng định nghĩa tên chủ đề cha để tránh gõ sai lệch
    private final String T_ANIMALS = "🐾 ANIMALS|CHỦ ĐỀ: ĐỘNG VẬT|Khám phá thế giới muông thú đáng yêu!";
    private final String T_DAILY = "🏠 DAILY LIFE|CHỦ ĐỀ: ĐỜI SỐNG|Những đồ vật quen thuộc quanh bé!";
    private final String T_SPORTS = "⚽ SPORTS|CHỦ ĐỀ: THỂ THAO|Cùng vận động để khỏe mạnh nào!";
    private final String T_EDUCATION = "🏫 EDUCATION|CHỦ ĐỀ: GIÁO DỤC|Hành trang bổ ích khi đến trường!";
    private final String T_TRANSPORT = "🚀 TRANSPORT|CHỦ ĐỀ: GIAO THÔNG|Vi vu trên các nẻo đường thôi bè ơi!";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocabulary, container, false);

        expandableListView = view.findViewById(R.id.vocabularyExpandableListView);
        fabAddVocabulary = view.findViewById(R.id.fabAddVocabulary);

        // 1. Nạp dữ liệu cứng mặc định
        initData();

        // 2. Kéo dữ liệu tự thêm của người dùng từ bộ nhớ máy lên (nếu có)
        loadUserCustomWords();

        // 3. Khởi tạo và liên kết adapter
        adapter = new VocabularyAdapter(requireContext(), listTopic, listWordsDetail);
        expandableListView.setAdapter(adapter);

        // 4. Bắt sự kiện click nút nổi để mở Form thêm từ
        fabAddVocabulary.setOnClickListener(v -> showAddWordDialog());

        return view;
    }

    private void initData() {
        listTopic = new ArrayList<>();
        listWordsDetail = new HashMap<>();

        listTopic.add(T_ANIMALS);
        List<VocabularyWord> animals = new ArrayList<>();
        animals.add(new VocabularyWord("Elephant", "Con voi", "The elephant has a very long trunk."));
        animals.add(new VocabularyWord("Monkey", "Con khỉ", "The funny monkey loves to eat bananas."));
        animals.add(new VocabularyWord("Giraffe", "Hươu cao cổ", "The giraffe has a very long neck."));
        animals.add(new VocabularyWord("Lion", "Sư tử", "The lion is the king of the jungle."));
        animals.add(new VocabularyWord("Tiger", "Con hổ", "The tiger has beautiful black stripes."));
        animals.add(new VocabularyWord("Dolphin", "Cá heo", "The clever dolphin jumps out of the water."));
        animals.add(new VocabularyWord("Penguin", "Chim cánh cụt", "The penguin walks slowly on the white snow."));
        animals.add(new VocabularyWord("Rabbit", "Con thỏ", "The cute rabbit loves eating crunchy carrots."));
        animals.add(new VocabularyWord("Kangaroo", "Con chuột túi", "The kangaroo carries its baby in a pouch."));
        animals.add(new VocabularyWord("Panda", "Gấu trúc", "The giant panda eats green bamboo leaves."));
        listWordsDetail.put(T_ANIMALS, animals);

        listTopic.add(T_DAILY);
        List<VocabularyWord> dailyLife = new ArrayList<>();
        dailyLife.add(new VocabularyWord("House", "Ngôi nhà", "We live happily in our sweet house."));
        dailyLife.add(new VocabularyWord("Bed", "Cái giường", "I go to bed early every night."));
        dailyLife.add(new VocabularyWord("Clock", "Cái đồng hồ", "The clock ticks and tells us the time."));
        dailyLife.add(new VocabularyWord("Table", "Cái bàn", "There is a glass of milk on the table."));
        dailyLife.add(new VocabularyWord("Chair", "Cái ghế", "Please sit down on this comfortable chair."));
        dailyLife.add(new VocabularyWord("Key", "Chìa khóa", "My father uses a key to open the door."));
        dailyLife.add(new VocabularyWord("Mirror", "Cái gương", "She looks at her smiling face in the mirror."));
        dailyLife.add(new VocabularyWord("Brush", "Bàn chải", "Remember to brush your teeth every morning."));
        dailyLife.add(new VocabularyWord("Spoon", "Cái muỗng / thìa", "I use a small spoon to eat my soup."));
        dailyLife.add(new VocabularyWord("Lamp", "Cái đèn", "The desk lamp helps me read books at night."));
        listWordsDetail.put(T_DAILY, dailyLife);

        listTopic.add(T_SPORTS);
        List<VocabularyWord> sports = new ArrayList<>();
        sports.add(new VocabularyWord("Football", "Môn bóng đá", "The boys love playing football together."));
        sports.add(new VocabularyWord("Swim", "Bơi lội", "I like to swim in the pool during summer."));
        sports.add(new VocabularyWord("Run", "Chạy bộ", "We run fast around the green park."));
        sports.add(new VocabularyWord("Bicycle", "Xe đạp", "He rides his new bicycle to school."));
        sports.add(new VocabularyWord("Badminton", "Môn cầu lông", "She plays badminton with her sister."));
        sports.add(new VocabularyWord("Tennis", "Môn quần vợt", "You need a racket to play tennis."));
        sports.add(new VocabularyWord("Basketball", "Môn bóng rổ", "He throws the ball into the basketball hoop."));
        sports.add(new VocabularyWord("Jump", "Nhảy dây", "The children jump rope on the playground."));
        sports.add(new VocabularyWord("Skate", "Trượt patin / ván", "It is fun to skate on the smooth path."));
        sports.add(new VocabularyWord("Winner", "Người chiến thắng", "The winner receives a shiny gold medal."));
        listWordsDetail.put(T_SPORTS, sports);

        listTopic.add(T_EDUCATION);
        List<VocabularyWord> education = new ArrayList<>();
        education.add(new VocabularyWord("School", "Trường học", "Our school has a big library."));
        education.add(new VocabularyWord("Teacher", "Thầy / Cô giáo", "The teacher reads an exciting story to us."));
        education.add(new VocabularyWord("Student", "Học sinh", "The student listens carefully in class."));
        education.add(new VocabularyWord("Book", "Quyển sách", "I read a colorful picture book every day."));
        education.add(new VocabularyWord("Pencil", "Bút chì", "I use a sharp pencil to draw a star."));
        education.add(new VocabularyWord("Ruler", "Cây thước kẻ", "Use a straight ruler to draw lines."));
        education.add(new VocabularyWord("Eraser", "Cục tẩy / gôm", "The pink eraser cleans up my mistakes."));
        education.add(new VocabularyWord("Classroom", "Lớp học", "Our classroom is bright and clean."));
        education.add(new VocabularyWord("Lesson", "Bài học", "Today's English lesson is very easy."));
        education.add(new VocabularyWord("Notebook", "Quyển vở / sổ", "I write my homework in the notebook."));
        listWordsDetail.put(T_EDUCATION, education);

        listTopic.add(T_TRANSPORT);
        List<VocabularyWord> transport = new ArrayList<>();
        transport.add(new VocabularyWord("Car", "Xe ô tô", "My family goes for a drive in our blue car."));
        transport.add(new VocabularyWord("Bus", "Xe buýt", "The yellow bus takes children to school."));
        transport.add(new VocabularyWord("Train", "Tàu hỏa", "The long train runs fast on the iron track."));
        transport.add(new VocabularyWord("Plane", "Máy bay", "The giant plane flies high in the blue sky."));
        transport.add(new VocabularyWord("Boat", "Con thuyền", "A small wooden boat sails on the river."));
        transport.add(new VocabularyWord("Motorbike", "Xe máy", "My mother rides a motorbike to the market."));
        transport.add(new VocabularyWord("Helicopter", "Máy bay trực thăng", "The helicopter can land on a small rooftop."));
        transport.add(new VocabularyWord("Truck", "Xe tải", "The big truck carries heavy boxes."));
        transport.add(new VocabularyWord("Subway", "Tàu điện ngầm", "The subway travels fast under the city ground."));
        transport.add(new VocabularyWord("Rocket", "Tên lửa", "The powerful rocket travels to the outer space."));
        listWordsDetail.put(T_TRANSPORT, transport);
    }

    // Hiển thị hộp thoại Form để điền từ vựng mới
    private void showAddWordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Sửa requireLayoutInflater() thành getLayoutInflater() là xong liền!
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_add_vocabulary, null);
        builder.setView(dialogView);

        EditText edtWord = dialogView.findViewById(R.id.edtNewWord);
        EditText edtMeaning = dialogView.findViewById(R.id.edtNewMeaning);
        EditText edtExample = dialogView.findViewById(R.id.edtNewExample);
        Spinner spinner = dialogView.findViewById(R.id.spinnerTopics);

        // Tạo danh sách text hiển thị ngắn gọn trên Dropdown cho người dùng dễ chọn
        List<String> displayDisplayTopics = new ArrayList<>();
        displayDisplayTopics.add("🐾 Động vật");
        displayDisplayTopics.add("🏠 Đời sống");
        displayDisplayTopics.add("⚽ Thể thao");
        displayDisplayTopics.add("🏫 Giáo dục");
        displayDisplayTopics.add("🚀 Giao thông");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, displayDisplayTopics);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        builder.setPositiveButton("Thêm ngay", (dialog, which) -> {
            String wordText = edtWord.getText().toString().trim();
            String meaningText = edtMeaning.getText().toString().trim();
            String exampleText = edtExample.getText().toString().trim();
            int selectedPosition = spinner.getSelectedItemPosition();

            if (wordText.isEmpty() || meaningText.isEmpty()) {
                Toast.makeText(getContext(), "Không được bỏ trống Từ hoặc Nghĩa đâu nhé!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (exampleText.isEmpty()) {
                exampleText = "No example available."; // Câu ví dụ dự phòng nếu bé lười nhập
            }

            // Ánh xạ ngược vị trí Spinner sang đúng mã Key lưu trữ của hệ thống
            String targetTopicKey = T_ANIMALS;
            if (selectedPosition == 1) targetTopicKey = T_DAILY;
            else if (selectedPosition == 2) targetTopicKey = T_SPORTS;
            else if (selectedPosition == 3) targetTopicKey = T_EDUCATION;
            else if (selectedPosition == 4) targetTopicKey = T_TRANSPORT;

            // Thêm từ mới vào mảng dữ liệu hiện tại trong bộ nhớ RAM
            VocabularyWord newWordObj = new VocabularyWord(wordText, meaningText, exampleText);
            List<VocabularyWord> targetList = listWordsDetail.get(targetTopicKey);
            if (targetList != null) {
                targetList.add(newWordObj);

                // Lưu từ này vĩnh viễn xuống bộ nhớ điện thoại (SharedPreferences)
                saveWordToLocal(targetTopicKey, wordText, meaningText, exampleText);

                // Ra lệnh Adapter cập nhật giao diện ngay lập tức
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Đã thêm từ '" + wordText + "' thành công!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Hàm lưu từ xuống máy để không bị mất khi thoát app
    private void saveWordToLocal(String topicKey, String word, String meaning, String example) {
        SharedPreferences pref = requireContext().getSharedPreferences("CustomVocabPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Lấy chuỗi dữ liệu đã lưu từ trước ra nối thêm từ mới vào (Chuỗi định dạng: từ_nghĩa_ví dụ###từ2_nghĩa2_ví dụ2)
        String currentData = pref.getString(topicKey, "");
        String newData = word + "&&" + meaning + "&&" + example;
        if (!currentData.isEmpty()) {
            currentData += "###" + newData;
        } else {
            currentData = newData;
        }

        editor.putString(topicKey, currentData);
        editor.apply();
    }

    // Hàm bốc các từ người dùng tự thêm từ SharedPreferences nhét lại vào list khi mở app
    private void loadUserCustomWords() {
        SharedPreferences pref = requireContext().getSharedPreferences("CustomVocabPrefs", Context.MODE_PRIVATE);
        String[] keys = {T_ANIMALS, T_DAILY, T_SPORTS, T_EDUCATION, T_TRANSPORT};

        for (String key : keys) {
            String rawData = pref.getString(key, "");
            if (!rawData.isEmpty()) {
                String[] wordsArray = rawData.split("###");
                List<VocabularyWord> targetList = listWordsDetail.get(key);

                if (targetList != null) {
                    for (String singleWordData : wordsArray) {
                        String[] items = singleWordData.split("&&");
                        if (items.length >= 3) {
                            targetList.add(new VocabularyWord(items[0], items[1], items[2]));
                        }
                    }
                }
            }
        }
    }
}