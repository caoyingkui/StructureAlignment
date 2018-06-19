package cn.edu.pku.sei.structureAlignment.util;

import javafx.util.Pair;
import org.eclipse.jdt.internal.core.util.Util;

import java.beans.Expression;
import java.io.*;
import java.util.*;

/**
 * Created by oliver on 2018/1/8.
 */
public class Matrix <T extends Valuable>{
    private List<List<T>> matrix;
    private int m;
    private int n;

    public int getM(){
        return m;
    }

    public int getN(){
        return n;
    }

    public Matrix(int m , int n , T t){
        this.m = m;
        this.n = n;
        matrix = new ArrayList<>();


        //深度拷贝
        try {


            for(int i = 0 ; i < m ; i ++) {
                List<T> row = new ArrayList<T>();
                for (int j = 0; j < n; j++){
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    ObjectOutputStream oo = new ObjectOutputStream(bo);
                    oo.writeObject(t);

                    ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
                    ObjectInputStream oi = new ObjectInputStream(bi);
                    row.add( (T)oi.readObject());
                }
                matrix .add(row);
            }
        }catch(Exception e){
            e.printStackTrace();
        }



    }

    public T getCell(int i, int j) {
        if(i < m && j < n)
            return  matrix.get(i).get(j);
        else
            return null;
    }

    public double getValue(int i , int j){
        if(i < m && j < n)
            return matrix.get(i).get(j).getValue();
        else
            return -1;
    }

    public void setCell(int i , int j , T cell){
        matrix.get(i).set(j , cell);
    }

    public void setValue(int i , int j , double value){
        matrix.get(i).get(j).setValue(value);
    }

    public void print(){
        Formatter formatter = new Formatter();
        formatter.format("%-3s" , " ");

        for(int j = 0 ; j < n ; j ++){
            formatter.format("%-3s " , j + 1 + "");
        }
        formatter.format("\n");

        for(int i = 0 ; i < m ; i ++){
            formatter.format("%-3s" , i + 1 + "");
            for(int j = 0 ; j < n ; j ++){
                T cell = matrix.get(i).get(j);
                if(cell != null)
                    formatter.format("%.1f " , cell.getValue());
                else
                    formatter.format("      ");

            }
            formatter.format("\n");
        }
        System.out.print(formatter);

    }

    public void print(double min){
        Formatter formatter = new Formatter(new StringBuilder( ), Locale.US);
        formatter.format("%-3s" , " ");

        for(int j = 0 ; j < n ; j ++){
            formatter.format("%-3s " , j  + 1 + "");
        }
        formatter.format("\n");

        for(int i = 0 ; i < m ; i ++){
            formatter.format("%-3s" , i + 1  + "");
            for(int j = 0 ; j < n ; j ++){
                T cell = matrix.get(i).get(j);
                if(cell != null && cell.getValue() > min){
                    formatter.format("%.1f " , cell.getValue());
                }else
                    formatter.format("    ");
            }
            formatter.format("\n");
        }

        System.out.print(formatter);
    }

    /**
     *
     * @param lowBound
     * @return 返回的是当前矩阵中最大值的的坐标值
     */
    public Pair<Integer , Integer> getMax(double lowBound){
        Pair<Integer , Integer> result = null;
        double max = -1;
        int max_row = -1;
        int max_column = -1;
        for(int i = 0 ; i < m ; i ++){
            for(int j = 0 ; j < n ; j ++){
                T cell = matrix.get(i).get(j);
                if(cell != null && cell.getValue() >= lowBound && cell.getValue() > max){
                    max = cell.getValue();
                    max_row = i;
                    max_column = j;
                }
            }
        }

        if(max_row != -1 ){
            result = new Pair<>(max_row , max_column);
        }
        return result;
    }

    /**
     * 该函数是用于获取column列中，大于等于lowRow的行中，最大值的的行下标
     * @param column
     * @param lowRow
     * @param lowBound
     * @return
     */
    public int getColumnMax(int column  , int lowRow , double lowBound){
        if(m < 1 || m <= lowRow || n <= column)
            return - 1;
        else {
            double max = matrix.get(lowRow).get(column).getValue();
            double temp;
            int result = 0;
            for (int i = lowRow + 1; i < m; i++) {
                temp = matrix.get(i).get(column).getValue();
                if(temp > max){
                    max = temp;
                    result = i;
                }
            }
            return max >= lowBound ? result : -1;
        }
    }

    public void cleanRow(int rowIndex){
        List<T> row = matrix.get(rowIndex);
        for(int i = 0 ; i < n ; i ++){
            row.set(i , null);
        }
        //print(0);
    }

    public void cleanColumn(int columnIndex){
        for(int i = 0 ; i < m ; i ++){
            List<T> row = matrix.get(i);
            row.set(columnIndex , null);
        }
        //print(0);
    }

    public List<Pair<Integer , Integer>> findBestMatchScheme(){
        int defaultBound = 2;
        return findBestMatchScheme(0 , 0 , m , n , defaultBound);
        //return findBestMatchScheme(defaultBound);
    }

    private List<Pair<Integer , Integer>> findBestMatchScheme(int m1 , int n1 , int m2 , int n2 , int bound){
        if(m1 < 0 || n1 < 0 || m2 < 0 || n2 < 0) return null;
        if(m1 >= m || n1 >= n || m2 > m || n2 > n ) return null;
        if(m1 >= m2 || n1 >= n2) return null;

        List<Pair<Integer , Integer>> result = null;
        double max = -1;
        int max_m = -1;
        int max_n = -1;



        // 从上到下，从左到右匹配
        for(int i = m1 ; i < m2 ; i ++){
            for(int j = n1 ; j < n2 ; j++){
                double point1 = (i * 1.0 - m1 + 1) / (m2 - m1);
                double point2 = (j * 1.0 - n1 + 1) / (n2 - n1);

                double temp = 0.1 * (1 - Math.abs(point1 - point2));
                temp += this.getCell(i , j).getValue();
                if(max < temp){
                    max = temp;
                    max_m = i;
                    max_n = j;
                }
            }
        }

        // 从下到上，从右到左匹配
        /*for(int i = m2 - 1 ; i >= m1 ; i --){
            for(int j = n2 - 1 ; j >= n1 ; j--){
                if(max < this.getCell(i , j).getValue()){
                    max = this.getCell(i , j ).getValue();
                    max_m = i;
                    max_n = j;
                }
            }
        }*/

        if(max >= bound){
            result = new ArrayList<>();
            result.add(new Pair<Integer , Integer>(max_m, max_n));

            List<Pair<Integer , Integer>> temp = findBestMatchScheme(m1 , n1 , max_m , max_n , bound);
            if(temp != null) result.addAll(temp);

            temp = findBestMatchScheme(max_m + 1 , max_n + 1 , m2 , n2 , bound);
            if(temp != null) result.addAll(temp);
        }

        if(result != null) {
            Collections.sort(result, new Comparator<Pair<Integer, Integer>>() {
                @Override
                public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                    return o1.getKey() - o2.getKey();
                }
            });
        }

        return result;
    }

    public List<Pair<Integer , Integer>> findBestMatchScheme(double bound){
        List<Pair<Integer , Integer>> result = new ArrayList<>();

        double temp[][] = new double[m][n];

        Pair<Integer ,Integer> path[][] = new Pair[m][n] ;
        int[][] signals = new int[m ][n];

        for(int i = 0 ; i < m ; i ++){
            for(int j = 0 ; j < n ; j ++){
                temp[i][j] = matrix.get(i).get(j).getValue();
                if(temp[i][j] < bound)
                    temp[i][j] = -1;
            }
        }

        signals[0][0] = temp[0][0] > 0 ? 0 : 5;
        int max_index = 0;
        for(int i = 1 ; i < m ; i ++){
            if(temp[i][0] > temp[max_index][0]){
                path[i][0] = new Pair<>(i , 0);
                max_index = i;
                signals[i][0] = 0;
            }else {
                path[i][0] = new Pair<>(max_index, 0);
                signals[i][0] = 4;
            }
        }

        max_index = 0 ;
        for(int j = 1 ; j < n ; j ++){
            if(temp[0][j] > temp[0][max_index]){
                path[0][j] = new Pair<>(0 , j);
                max_index = j;
                signals[0][j] = 0;
            }else {
                path[0][j] = new Pair<>(0, max_index);
                signals[0][j] = 3;
            }
        }

        int signal = 0;
        double max = -1;
        for(int i = 1 ; i < m ; i ++){
            for(int j = 1 ; j < n ; j ++){
                // signal : 0, 代表最大值只取temp[i][j]
                //          1, 代表最大值只取temp[i - 1][j - 1]
                //          2, 代表最大值取temp[i][j] + temp[i - 1][j - 1]
                //          3，代表取同行的左侧某个值
                //          4, 代表取同列的上侧某个值
                //          5, 代表该矩阵不存在最优选择

                signal = 5;
                max = 0;

                if(temp[i][j] > max){
                    max = temp[i][j] ;
                    signal = 0;
                }

                if(temp[i - 1][j - 1] > 0){
                    max += temp[i - 1][j - 1];
                    signal = signal == 0 ? 2 : 1;
                }

                int m_index = -1, n_index = -1;
                for(int k = 0 ; k < j ; k ++){
                    if(temp[i][k] > max){
                        max = temp[i][k];
                        m_index = i;
                        n_index = k;
                        signal = 3;
                    }
                }

                for(int k = 0 ; k < i ; k ++){
                    if(temp[k][j] > max){
                        max = temp[k][j];
                        m_index = k;
                        n_index = j;
                        signal = 4;
                    }
                }
                signals[i][j] = signal;
                if(signal == 3 || signal == 4){
                    path[i][j] = new Pair<>(m_index , n_index);
                }

                temp[i][j] = max;
            }
        }

        int m_index = m - 1, n_index = n - 1;
        while(signals[m_index][n_index] != 5){
            Pair<Integer ,Integer> pair = path[m_index][n_index];
            switch (signals[m_index][n_index]){
                case 0:{
                    result.add(new Pair<>(m_index , n_index));
                    return result;
                }case 1:{
                    m_index --;
                    n_index --;
                    break;
                }case 2:{
                    result.add(new Pair<>(m_index , n_index));
                    m_index --;
                    n_index --;
                    break;
                }case 3:{
                    m_index = pair.getKey();
                    n_index = pair.getValue();
                    break;
                }case 4:{
                    m_index = pair.getKey();
                    n_index = pair.getValue();
                    break;
                }case 5:{
                    return null;
                }

            }
        }



        return null;
    }

    public double similarity(Map<Pair<Integer , Integer> , Double> matchedNodes){
        double result = 0 ;
        Pair<Integer , Integer> max;
        while((max = getMax(0.01)) != null){
            int codeId = max.getKey();
            int textId = max.getValue();
            double sim = 4 * getCell(codeId , textId).getValue();
            result += sim;
            cleanRow(codeId);
            cleanColumn( textId);

            if(matchedNodes != null){
                matchedNodes.put(new Pair<Integer , Integer>(codeId , textId) , sim);
            }
        }
        return result;
    }
}
