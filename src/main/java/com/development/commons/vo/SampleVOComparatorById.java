package com.development.commons.vo;

import java.util.Comparator;

public class SampleVOComparatorById implements Comparator<SampleVO> {

    private boolean isAscending;

    public SampleVOComparatorById(boolean isAscending) {
        this.isAscending = isAscending;
    }

    public int compare(SampleVO o1, SampleVO o2) {
        int result;
        if (o1 != null && o2 != null) {
            if (o1.getId() == null && o2.getId() != null) {
                result = -1;
            } else if (o1.getId() != null && o2.getId() == null) {
                result = 1;
            } else {
                result = o1.getId().compareTo(o2.getId());
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
