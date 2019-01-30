package org.du.interview.pingcap.sort;

@FunctionalInterface
public interface LongComparator {

    int compare(long l1, long l2);

}
