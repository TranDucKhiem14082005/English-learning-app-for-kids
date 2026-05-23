package khiem.it.tinyenglish.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.model.VocabularyWord;

public class VocabularyAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> listTopic;
    private final Map<String, List<VocabularyWord>> listWordsDetail;

    private final int[] pastelColors = {
            0xFFFFF2CC, // Vàng nhạt
            0xFFE2EFDA, // Xanh lá nhạt
            0xFFFCE4D6, // Cam đào nhạt
            0xFFD9E1F2, // Xanh dương nhạt
            0xFFE1D5E7  // Tím nhạt
    };

    public VocabularyAdapter(Context context, List<String> listTopic, Map<String, List<VocabularyWord>> listWordsDetail) {
        this.context = context;
        this.listTopic = listTopic;
        this.listWordsDetail = listWordsDetail;
    }

    @Override
    public int getGroupCount() {
        return listTopic.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<VocabularyWord> children = listWordsDetail.get(listTopic.get(groupPosition));
        return children != null ? children.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listTopic.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<VocabularyWord> children = listWordsDetail.get(listTopic.get(groupPosition));
        return children != null ? children.get(childPosition) : null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    // hiển thị GIAO DIỆN CHỦ ĐỀ CHA (Dropdown Header)
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String topicInfo = (String) getGroup(groupPosition);
        GroupViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_vocabulary_group, parent, false);
            holder = new GroupViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        // Tách chuỗi để lấy Icon, Tên và Mô tả chủ đề
        String[] parts = topicInfo.split("\\|");
        if (parts.length >= 3) {
            holder.titleText.setText(parts[0]);
            holder.descText.setText(parts[2]);
        } else {
            holder.titleText.setText(topicInfo);
        }

        // Tự động xoay chiều mũi tên tùy thuộc vào trạng thái đang đóng hay đang mở
        if (isExpanded) {
            holder.indicator.setText("▲"); // Mở ra thì quay lên
        } else {
            holder.indicator.setText("▼"); // Thu lại thì quay xuống
        }

        return convertView;
    }

    // hiển thị GIAO DIỆN TỪ VỰNG CON (Child Item)
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
            holder = new ChildViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        VocabularyWord word = (VocabularyWord) getChild(groupPosition, childPosition);
        if (word != null) {
            holder.wordText.setText(word.getWord());
            holder.meaningText.setText(word.getMeaning());
            holder.exampleText.setText(word.getExample());

            // Đổ màu nền Pastel xoay vòng sinh động cho thẻ Card từ vựng
            int randomColor = pastelColors[childPosition % pastelColors.length];
            holder.card.setCardBackgroundColor(randomColor);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView titleText;
        TextView descText;
        TextView indicator;

        GroupViewHolder(View view) {
            titleText = view.findViewById(R.id.groupTitleText);
            descText = view.findViewById(R.id.groupDescText);
            indicator = view.findViewById(R.id.groupIndicator);
        }
    }

    static class ChildViewHolder {
        MaterialCardView card;
        TextView wordText;
        TextView meaningText;
        TextView exampleText;
        TextView speakerIcon;

        ChildViewHolder(View view) {
            card = view.findViewById(R.id.vocabCard);
            wordText = view.findViewById(R.id.vocabWordText);
            meaningText = view.findViewById(R.id.vocabMeaningText);
            exampleText = view.findViewById(R.id.vocabExampleText);
            speakerIcon = view.findViewById(R.id.vocabSpeakerIcon);
        }
    }
}