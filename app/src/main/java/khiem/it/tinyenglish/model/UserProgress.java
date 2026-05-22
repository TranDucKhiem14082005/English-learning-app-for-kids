package khiem.it.tinyenglish.model;

public class UserProgress {
    private String userId;
    private String lessonId;
    private int score;
    private boolean completed;
    private long lastAttemptDate;

    public UserProgress() {
    }

    public UserProgress(String userId, String lessonId, int score, boolean completed, long lastAttemptDate) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.score = score;
        this.completed = completed;
        this.lastAttemptDate = lastAttemptDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getLastAttemptDate() {
        return lastAttemptDate;
    }

    public void setLastAttemptDate(long lastAttemptDate) {
        this.lastAttemptDate = lastAttemptDate;
    }
}
