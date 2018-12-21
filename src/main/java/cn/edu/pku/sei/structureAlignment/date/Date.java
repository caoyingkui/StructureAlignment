package cn.edu.pku.sei.structureAlignment.date;

/**
 * Created by oliver on 2018/7/31.
 */
public class Date {
    private java.util.Date oldTime = new java.util.Date();
    private java.util.Date newTime;
    public void start(){
        oldTime = new java.util.Date();
    }

    public long difference(){
        newTime = new java.util.Date();
        long difference = newTime.getTime() - oldTime.getTime();
        oldTime = newTime;
        return difference;
    }
}
