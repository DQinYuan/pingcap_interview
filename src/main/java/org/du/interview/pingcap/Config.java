package org.du.interview.pingcap;

public interface Config {

    //测试4 * 1024
    //3G
    long CONTENT_SIZE = 3 * 1024 * 1024 * 1024;

    int READ_BUFFER_SIZE = 4 * 1024;

    int WRITE_BUFFER_SIZE = 4 * 1024;

    String USER_ORDER_FILE = "user_order.dat";

    String ITEM_HASH_FILE = "item_hash.dat";

}
