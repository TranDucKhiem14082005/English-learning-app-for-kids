package khiem.it.tinyenglish.model;

public class VocabularyWord {
    private final String word;
    private final String meaning;
    private final String example;

    public VocabularyWord(String word, String meaning, String example) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getExample() {
        return example;
    }
}
