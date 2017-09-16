package com.joblancr.cards;

/**
 * Created by Faruk on 10/6/16.
 */
public class States {
    private int id;
    private String name;

    public States(){}

    public States(int id, String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
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
