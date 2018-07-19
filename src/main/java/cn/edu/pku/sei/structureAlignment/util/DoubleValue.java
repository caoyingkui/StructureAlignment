package cn.edu.pku.sei.structureAlignment.util;

/**
 * Created by oliver on 2018/1/10.
 */
public class DoubleValue implements Valuable{
    double value ;
    String logInfo;

    public DoubleValue(double value){
        this.value = value;
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
    public String getLogInfo(){
        return logInfo;
    }

    @Override
    public void setLogInfo(String string){
        logInfo = string;
    }

}
