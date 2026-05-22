package khiem.it.tinyenglish.model;

import java.util.List;

public class Question {
    private String id;
    private String lessonId;
    private String questionText;
    private String questionType; // MULTIPLE_CHOICE or TRUE_FALSE
    private List<String> options;
    private String correctAnswer;
    private int order;
    private String imageUrl;
    private String explanation;

    public Question() {
    }

    public Question(String id, String lessonId, String questionText, String questionType,
                    List<String> options, String correctAnswer, int order) {
        this.id = id;
        this.lessonId = lessonId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.order = order;
        this.imageUrl = "";
        this.explanation = "";
    }

    public Question(String id, String lessonId, String questionText, String questionType,
                    List<String> options, String correctAnswer, int order, String imageUrl) {
        this.id = id;
        this.lessonId = lessonId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.order = order;
        this.imageUrl = imageUrl;
        this.explanation = "";
    }

    public Question(String id, String lessonId, String questionText, String questionType,
                    List<String> options, String correctAnswer, int order, String imageUrl, String explanation) {
        this.id = id;
        this.lessonId = lessonId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.order = order;
        this.imageUrl = imageUrl;
        this.explanation = explanation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
