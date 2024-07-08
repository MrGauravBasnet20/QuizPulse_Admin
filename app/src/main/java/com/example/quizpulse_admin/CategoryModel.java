package com.example.quizpulse_admin;

// CategoryModel class represents a model for a category in the application.
public class CategoryModel {
    // Fields to store information about the category: image URL and title.
    private String name;
    private int sets;
    private String url;
    
    private String key;

    private CategoryModel()
    {
        //for firebase
    }
    public CategoryModel(String name, int sets, String url, String key) {
        this.name = name;
        this.sets = sets;
        this.url = url;
        this.key=key;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
