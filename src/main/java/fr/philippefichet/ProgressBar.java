/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet;

/**
 *
 * @author glopinous
 */
public class ProgressBar {
    private long value;
    private long max;
    private String progressBar;
    private int size;

    public ProgressBar(long value, long max, int size) {
        this.value = value;
        this.max = max;
        this.size = size;
    }

    public long getValue() {
        return value;
    }

    public long getMax() {
        return max;
    }

    public String getProgressBar() {
        int sizeValue = (int) (((float)value / (float)max)*(size));
        if (progressBar == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < size - 2; i++) {
                if (i <= (sizeValue - 2)) {
                    sb.append("|");
                } else {
                    sb.append(" ");
                }
            }
            sb.append("]");
            progressBar = sb.toString();
        }
        return progressBar;
    }

    public int getSize() {
        return size;
    }
}
