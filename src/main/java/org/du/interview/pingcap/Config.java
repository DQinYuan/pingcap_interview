package org.du.interview.pingcap;

public interface Config {

    //为了便于测试,先使用 4 * 1024
    //1.5G   1610612736
    int CONTENT_SIZE = 4 * 1024;

    int READ_BUFFER_SIZE = 4 * 1024;

    int WRITE_BUFFER_SIZE = 4 * 1024;

    int READ_BUFFER_NUM = CONTENT_SIZE / READ_BUFFER_SIZE;

    String USER_ORDER_FILE = "user_order.dat";

    String ITEM_HASH_FILE = "item_hash.dat";

}
