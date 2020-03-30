package com.example.aplikasita.storage;

public class FilterStorage {

    public FilterStorage(){

    }

    // variable
    private int dehazeLevel;
    private int brightLevel;
    private int satLevel;
    private int contrastLevel;

    public int getDehazeLevel() {
        return dehazeLevel;
    }

    public void setDehazeLevel(int dehazeLevel) {
        this.dehazeLevel = dehazeLevel;
    }

    public int getBrightLevel() {
        return brightLevel;
    }

    public void setBrightLevel(int brightLevel) {
        this.brightLevel = brightLevel;
    }

    public int getSatLevel() {
        return satLevel;
    }

    public void setSatLevel(int satLevel) {
        this.satLevel = satLevel;
    }

    public int getContrastLevel() {
        return contrastLevel;
    }

    public void setContrastLevel(int contrastLevel) {
        this.contrastLevel = contrastLevel;
    }

}
