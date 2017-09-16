package com.joblancr.cards;

/**
 * Created by Faruk on 10/6/16.
 */
public class SelectCategoryCard {
    private int photoId;
    private int id;
    private String name;

    public SelectCategoryCard(int photoId, int id, String name){
        setPhotoId(photoId);
        setId(id);
        setName(name);
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getPhotoId() {
        return this.photoId;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }
}
