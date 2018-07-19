package cn.edu.pku.sei.structureAlignment.util;

import java.io.Serializable;

/**
 * Created by oliver on 2018/1/10.
 */
public interface Valuable extends Serializable{
    double getValue();
    void setValue(double value);
    String getLogInfo();
    void setLogInfo(String value);
}
