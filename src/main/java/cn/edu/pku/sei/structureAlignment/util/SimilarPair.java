package cn.edu.pku.sei.structureAlignment.util;

/**
 * Created by oliver on 2018/1/10.
 */
public class SimilarPair implements Valuable{
    public int left;
    public int right;
    double value;

    public SimilarPair(int left , int right){
        this.left = left;
        this.right = right;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String getLogInfo() {
        return null;
    }

    @Override
    public void setLogInfo(String value) {

    }
}
