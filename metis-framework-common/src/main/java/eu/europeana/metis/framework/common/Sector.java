package eu.europeana.metis.framework.common;

/**
 * Created by ymamakis on 4/4/16.
 */
public enum Sector {

    GOVT_MINISTRY("Government Department / Ministry"),PRIVATE("Private"),PUBLIC("Public");

    private String name;
    Sector(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
