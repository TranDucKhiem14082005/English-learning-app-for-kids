package khiem.it.tinyenglish.model;

public class VocabularyWord {
    // 1. Loại bỏ hoàn toàn từ khóa 'final' để cho phép Firebase ghi đè dữ liệu động
    private String word;
    private String meaning;
    private String example;

    // 2. BẮT BUỘC KHÔNG ĐƯỢC THIẾU: Constructor không tham số (No-argument constructor) dành riêng cho Firebase
    public VocabularyWord() {
        // Để trống hoàn toàn theo đúng quy chuẩn ClassMapper của Google Firebase
    }

    // 3. Constructor đầy đủ tham số dùng để Seeder nạp dữ liệu mẫu ban đầu
    public VocabularyWord(String word, String meaning, String example) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
    }

    // 4. Hệ thống các hàm Getter và Setter chuẩn chỉ
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}