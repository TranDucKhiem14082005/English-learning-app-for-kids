package khiem.it.tinyenglish.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.model.VocabularyWord;

public class VocabularyAdapter extends BaseAdapter {

    private final Context context;
    private final List<VocabularyWord> words;

    public VocabularyAdapter(Context context, List<VocabularyWord> words) {
        this.context = context;
        this.words = words;
    }

    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public Object getItem(int position) {
        return words.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VocabularyWord word = words.get(position);
        holder.word.setText(word.getWord());
        holder.meaning.setText(word.getMeaning());
        holder.example.setText(word.getExample());
        return convertView;
    }

    static class ViewHolder {
        TextView word;
        TextView meaning;
        TextView example;

        ViewHolder(View view) {
            word = view.findViewById(R.id.vocabWordText);
            meaning = view.findViewById(R.id.vocabMeaningText);
            example = view.findViewById(R.id.vocabExampleText);
        }
    }
}
