package khiem.it.tinyenglish.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private DatabaseReference topicsRef;
    private DatabaseReference wordsRef;

    // Bộ nhớ đệm dùng để ánh xạ ngược mã Key ngẫu nhiên với chuỗi hiển thị thực tế trên UI
    private Map<String, String> customTopicKeyMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocabulary, container, false);

        expandableListView = view.findViewById(R.id.vocabularyExpandableListView);
        fabAddVocabulary = view.findViewById(R.id.fabAddVocabulary);

        listTopic = new ArrayList<>();
        listWordsDetail = new HashMap<>();
        customTopicKeyMap = new HashMap<>(); // Khởi tạo bộ nhớ đệm ánh xạ

        topicsRef = FirebaseDatabase.getInstance().getReference("vocabulary_topics");
        wordsRef = FirebaseDatabase.getInstance().getReference("vocabulary_words");

        adapter = new VocabularyAdapter(requireContext(), listTopic, listWordsDetail);
        expandableListView.setAdapter(adapter);

        khiem.it.tinyenglish.util.FirebaseDataSeeder.seedDataIfNeeded(requireContext());

        loadFirebaseVocabularyData();

        fabAddVocabulary.setOnClickListener(v -> showAddWordDialog());

        return view;
    }

    private void loadFirebaseVocabularyData() {
        topicsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listTopic.clear();
                customTopicKeyMap.clear(); // Làm sạch cache ánh xạ mã tự chế

                for (DataSnapshot topicSnapshot : snapshot.getChildren()) {
                    String topicKey = topicSnapshot.getKey(); // Bốc lấy Key của nhóm (Vd: "custom_17173...")
                    String topicInfo = null;

                    if (topicSnapshot.getValue() instanceof String) {
                        topicInfo = topicSnapshot.getValue(String.class);
                    } else {
                        Object value = topicSnapshot.getValue();
                        if (value != null) topicInfo = value.toString();
                    }

                    if (topicInfo != null && topicKey != null) {
                        listTopic.add(topicInfo);

                        // Lưu vết mối quan hệ: "custom_17173..." ứng với chuỗi "📝 Tên chủ đề|CHỦ ĐỀ TỰ TẠO|..."
                        if (topicKey.startsWith("custom_")) {
                            customTopicKeyMap.put(topicKey, topicInfo);
                        }

                        if (!listWordsDetail.containsKey(topicInfo)) {
                            listWordsDetail.put(topicInfo, new ArrayList<>());
                        }
                    }
                }
                loadFirebaseWordsDetail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) Toast.makeText(getContext(), "Lỗi kết nối Firebase!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFirebaseWordsDetail() {
        wordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String topic : listTopic) {
                    List<VocabularyWord> words = listWordsDetail.get(topic);
                    if (words != null) words.clear();
                }

                for (DataSnapshot wordGroupSnapshot : snapshot.getChildren()) {
                    String groupKey = wordGroupSnapshot.getKey();
                    if (groupKey == null) continue;

                    String matchedTopicInfo = null;

                    // KHẮC PHỤC LỖI: Kiểm tra xem nếu Key thuộc nhóm tự tạo thì lấy từ Map ánh xạ ra ngay lập tức
                    if (groupKey.startsWith("custom_") && customTopicKeyMap.containsKey(groupKey)) {
                        matchedTopicInfo = customTopicKeyMap.get(groupKey);
                    } else {
                        // Nếu là nhóm hệ thống sẵn có thì áp dụng thuật toán so khớp chuỗi cũ của anh
                        for (String topic : listTopic) {
                            String cleanTopicNameInList = topic.split("\\|")[0].replaceAll("[.#$\\[\\]]", "").trim();

                            if (groupKey.equalsIgnoreCase(cleanTopicNameInList) ||
                                    (groupKey.equals("topic_animals") && cleanTopicNameInList.equals("🐾 ANIMALS")) ||
                                    (groupKey.equals("topic_daily") && cleanTopicNameInList.equals("🏠 DAILY LIFE")) ||
                                    (groupKey.equals("topic_sports") && cleanTopicNameInList.equals("⚽ SPORTS")) ||
                                    (groupKey.equals("topic_education") && cleanTopicNameInList.equals("🏫 EDUCATION")) ||
                                    (groupKey.equals("topic_transport") && cleanTopicNameInList.equals("🚀 TRANSPORT"))) {

                                matchedTopicInfo = topic;
                                break;
                            }
                        }
                    }

                    if (matchedTopicInfo != null) {
                        List<VocabularyWord> targetList = listWordsDetail.get(matchedTopicInfo);
                        if (targetList != null) {
                            for (DataSnapshot singleWordSnapshot : wordGroupSnapshot.getChildren()) {
                                VocabularyWord word = singleWordSnapshot.getValue(VocabularyWord.class);
                                if (word != null) {
                                    targetList.add(word);
                                }
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddWordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_vocabulary, null);
        builder.setView(dialogView);

        EditText edtWord = dialogView.findViewById(R.id.edtNewWord);
        EditText edtMeaning = dialogView.findViewById(R.id.edtNewMeaning);
        EditText edtExample = dialogView.findViewById(R.id.edtNewExample);
        Spinner spinner = dialogView.findViewById(R.id.spinnerTopics);
        EditText edtCustomTopic = dialogView.findViewById(R.id.edtCustomTopic);

        List<String> spinnerOptions = new ArrayList<>();
        for (String topic : listTopic) {
            String[] parts = topic.split("\\|");
            spinnerOptions.add(parts[0]);
        }
        spinnerOptions.add("➕ [Tự nhập chủ đề mới bằng tay...]");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == spinnerOptions.size() - 1) {
                    edtCustomTopic.setVisibility(View.VISIBLE);
                } else {
                    edtCustomTopic.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setPositiveButton("Thêm ngay", (dialog, which) -> {
            String wordText = edtWord.getText().toString().trim();
            String meaningText = edtMeaning.getText().toString().trim();
            String exampleText = edtExample.getText().toString().trim();
            int selectedPosition = spinner.getSelectedItemPosition();

            if (wordText.isEmpty() || meaningText.isEmpty()) {
                Toast.makeText(getContext(), "Không được bỏ trống Từ hoặc Nghĩa đâu nhé!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (exampleText.isEmpty()) exampleText = "No example available.";

            VocabularyWord newWordObj = new VocabularyWord(wordText, meaningText, exampleText);

            if (selectedPosition == spinnerOptions.size() - 1) {
                String customTopicName = edtCustomTopic.getText().toString().trim();
                if (customTopicName.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng điền tên chủ đề mới!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String cleanNodeKey = "custom_" + System.currentTimeMillis();
                String fullTopicValue = "📝 " + customTopicName + "|CHỦ ĐỀ TỰ TẠO|Danh sách từ vựng do bạn biên soạn!";

                // Tiến hành đồng bộ hóa dữ liệu gối đầu trực tiếp lên đám mây
                topicsRef.child(cleanNodeKey).setValue(fullTopicValue).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        wordsRef.child(cleanNodeKey).child("0").setValue(newWordObj);
                        if (isAdded()) Toast.makeText(getContext(), "Đã khởi tạo nhóm và thêm từ vựng thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                String fullSelectedTopic = listTopic.get(selectedPosition);
                String cleanNodeKey = fullSelectedTopic.split("\\|")[0].replaceAll("[.#$\\[\\]]", "").trim();

                if (cleanNodeKey.equals("🐾 ANIMALS")) cleanNodeKey = "topic_animals";
                else if (cleanNodeKey.equals("🏠 DAILY LIFE")) cleanNodeKey = "topic_daily";
                else if (cleanNodeKey.equals("⚽ SPORTS")) cleanNodeKey = "topic_sports";
                else if (cleanNodeKey.equals("🏫 EDUCATION")) cleanNodeKey = "topic_education";
                else if (cleanNodeKey.equals("🚀 TRANSPORT")) cleanNodeKey = "topic_transport";

                List<VocabularyWord> currentList = listWordsDetail.get(fullSelectedTopic);
                int nextIndex = (currentList != null) ? currentList.size() : 0;

                wordsRef.child(cleanNodeKey).child(String.valueOf(nextIndex)).setValue(newWordObj).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        Toast.makeText(getContext(), "Đã nạp từ vựng mới lên đám mây thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}