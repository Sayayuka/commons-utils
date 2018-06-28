package com.development.commons.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.development.commons.utility.comparator.CompositeComparator;
import com.development.commons.vo.SampleVO;
import com.development.commons.vo.SampleVOComparatorById;
import com.development.commons.vo.SampleVOComparatorByName;

public class CommonUtils {

    /**
     * Paginate objects by $skip, $top attributes
     *
     * @param objects
     * @param skip
     * @param top
     * @return List<RuleParameterInfo>
     */
    public static List pagination(List objects, int skip, int top) {
        List paginated = new ArrayList();
        Iterator<?> tr = objects.iterator();
        while (tr.hasNext() && skip > 0) {
            tr.next();
            tr.remove();
            skip--;
        }

        Iterator at = objects.iterator();
        while (at.hasNext() && top > 0) {
            paginated.add(at.next());
            top--;
        }
        return paginated;
    }

    public static void sort(List<SampleVO> sampleVOs, boolean isAsc) {
        List<Comparator<SampleVO>> comparators = new ArrayList<Comparator<SampleVO>>();
        comparators.add(new SampleVOComparatorById(isAsc));
        comparators.add(new SampleVOComparatorByName(isAsc));
        Collections.sort(sampleVOs, new CompositeComparator(comparators));
    }

}
