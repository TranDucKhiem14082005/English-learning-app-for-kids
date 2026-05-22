package khiem.it.tinyenglish.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.adapter.VocabularyAdapter;
import khiem.it.tinyenglish.model.VocabularyWord;

public class VocabularyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocabulary, container, false);
        ListView listView = view.findViewById(R.id.vocabularyListView);
        listView.setAdapter(new VocabularyAdapter(requireContext(), createWords()));
        return view;
    }

    private List<VocabularyWord> createWords() {
        List<VocabularyWord> words = new ArrayList<>();
        words.add(new VocabularyWord("Apple", "Quả táo", "This apple is red."));
        words.add(new VocabularyWord("Book", "Quyển sách", "I read a book every night."));
        words.add(new VocabularyWord("Cat", "Con mèo", "The cat is sleeping."));
        words.add(new VocabularyWord("Dog", "Con chó", "My dog is very friendly."));
        words.add(new VocabularyWord("Happy", "Vui vẻ", "She feels happy today."));
        return words;
    }
}
