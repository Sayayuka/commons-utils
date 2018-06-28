package com.development.commons.vo;

import java.util.Comparator;

public class SampleVOComparatorByName implements Comparator<SampleVO> {

    private boolean isAscending;

    public SampleVOComparatorByName(boolean isAscending) {
        this.isAscending = isAscending;
    }

    public int compare(SampleVO o1, SampleVO o2) {
        int result;
        if (o1 != null && o2 != null) {
            if (o1.getName() == null && o2.getName() != null) {
                result = -1;
            } else if (o1.getName() != null && o2.getName() == null) {
                result = 1;
            } else {
                result = o1.getName().compareTo(o2.getName());
            }
        } else if (o1 == null && o2 != null) {
            result = -1;
        } else if (o1 != null) {
            result = 1;
        } else {
            result = 0;
        }
        return isAscending ? result : -result;
    }

}
