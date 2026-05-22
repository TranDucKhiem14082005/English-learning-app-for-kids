package khiem.it.tinyenglish.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import khiem.it.tinyenglish.LessonDetailActivity;
import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.model.Lesson;
import khiem.it.tinyenglish.model.UserProgress;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private static final String TAG = "LessonAdapter";
    private final List<Lesson> lessons;
    private final Context context;
    private final DatabaseReference progressRef;

    // Sử dụng Map để lưu trữ trạng thái tiến độ realtime của từng bài học, tránh lỗi tái sử dụng View của RecyclerView
    private final Map<String, UserProgress> progressMap = new HashMap<>();

    public LessonAdapter(List<Lesson> lessons, Context context) {
        this.lessons = lessons;
        this.context = context;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.progressRef = FirebaseDatabase.getInstance().getReference("userProgress")
                    .child(currentUser.getUid());
            // Khởi chạy lắng nghe cập nhật tiến độ Realtime từ Firebase
            observeUserProgress();
        } else {
            this.progressRef = null;
        }
    }

    // Hàm lắng nghe tiến độ tự động cập nhật giao diện ngay khi có thay đổi dữ liệu từ màn hình khác
    private void observeUserProgress() {
        if (progressRef == null) return;

        progressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressMap.clear();
                for (DataSnapshot lessonProgressSnapshot : snapshot.getChildren()) {
                    UserProgress progress = lessonProgressSnapshot.getValue(UserProgress.class);
                    if (progress != null) {
                        progressMap.put(lessonProgressSnapshot.getKey(), progress);
                    }
                }
                // Ra lệnh cho toàn bộ danh sách vẽ lại giao diện với dữ liệu mới nhất
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi lắng nghe cập nhật tiến độ: " + error.getMessage());
            }
        });
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.title.setText(lesson.getTitle());
        holder.description.setText(lesson.getDescription());
        holder.emoji.setText(lesson.getEmoji());

        // Lấy tiến độ thực tế của bài học này ra để tính toán trạng thái đóng/mở khóa
        UserProgress progress = progressMap.get(lesson.getId());

        // CHỈNH SỬA QUAN TRỌNG: Định nghĩa một bài học ĐƯỢC MỞ KHÓA khi thỏa mãn 1 trong các điều kiện:
        // 1. Là bài học đầu tiên (position == 0)
        // 2. Thuộc tính unlocked mặc định trên Firebase của bài đó bằng true
        // 3. Đã có bản ghi tiến độ tồn tại trong node userProgress (chính là bản ghi được hàm unlockNextLesson tạo ra)
        boolean isCurrentLessonUnlocked = lesson.isUnlocked() || position == 0 || progress != null;

        // Cập nhật giao diện Trạng thái & Điểm số tương ứng
        if (progress != null) {
            holder.score.setText("Score: " + progress.getScore() + "/15");

            // Đổi isPassed() thành tên hàm check boolean chuẩn trong Model của bạn (isCompleted() hoặc isPassed())
            if (progress.isCompleted()) {
                holder.status.setText("✓ Hoàn thành");
                // SỬA TẠI ĐÂY: Ép màu chữ thành MÀU TRẮNG để nổi bật trên nền xanh lá
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            } else {
                holder.status.setText("Sẵn sàng");
                // SỬA TẠI ĐÂY: Ép màu chữ thành MÀU TRẮNG để nổi bật trên nền xanh lá
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            }
        } else {
            holder.score.setText("Score: 0/15");
            if (isCurrentLessonUnlocked) {
                holder.status.setText("Sẵn sàng");
                // SỬA TẠI ĐÂY: Ép màu chữ thành MÀU TRẮNG để nổi bật trên nền xanh lá
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            } else {
                holder.status.setText("Khóa");
                // Đối với nút Khóa, nếu nền nút màu khác (ví dụ màu xám hoặc xanh nhạt) thì bạn giữ màu cũ
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            }
        }

        // CHỈNH SỬA QUAN TRỌNG: Logic xử lý Click đồng bộ với trạng thái thực tế
        holder.card.setOnClickListener(v -> {
            if (isCurrentLessonUnlocked) {
                Intent intent = new Intent(context, LessonDetailActivity.class);
                intent.putExtra("lessonId", lesson.getId());
                intent.putExtra("lessonTitle", lesson.getTitle());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Lesson bị khoá. Hoàn thành bài học trước đó!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        TextView emoji;
        TextView status;
        TextView score;
        MaterialCardView card;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.lessonTitleText);
            description = itemView.findViewById(R.id.lessonDescriptionText);
            emoji = itemView.findViewById(R.id.lessonEmoji);
            status = itemView.findViewById(R.id.lessonStatus);
            score = itemView.findViewById(R.id.lessonScore);
            card = (MaterialCardView) itemView;
        }
    }
}