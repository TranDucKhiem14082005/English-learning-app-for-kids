package khiem.it.tinyenglish.model;

public class Lesson {
    private String id;
    private String title;
    private String description;
    private String emoji;
    private int totalQuestions;
    private boolean unlocked;

    public Lesson() {
    }

    public Lesson(String id, String title, String description, String emoji, int totalQuestions, boolean unlocked) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.emoji = emoji;
        this.totalQuestions = totalQuestions;
        this.unlocked = unlocked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
