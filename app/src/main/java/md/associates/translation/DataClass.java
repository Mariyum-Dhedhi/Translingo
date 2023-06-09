package md.associates.translation;

public class DataClass {
    private String generatedText;
    private String key;

    public DataClass(String generatedText) {
        this.generatedText = generatedText;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DataClass(){
    }
}
