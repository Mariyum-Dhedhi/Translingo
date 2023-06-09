package md.associates.translation;

import java.io.Serializable;

public class ItemsModel implements Serializable {

    private String languages;
    private int languageImages;

    public ItemsModel(String name,int images) {
        this.languages = name;
        this.languageImages = images;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public int getImages() {
        return languageImages;
    }

    public void setImages(int images) {
        this.languageImages = images;
    }
}