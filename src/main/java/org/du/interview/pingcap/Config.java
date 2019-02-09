package org.du.interview.pingcap;

public interface Config {

    //测试4 * 1024
    //3G 3L * 1024L * 1024L * 1024L
    long CONTENT_SIZE = 3L * 1024L * 1024L * 1024L;

    int READ_BUFFER_SIZE = 4 * 1024;

    int WRITE_BUFFER_SIZE = 4 * 1024;

    String USER_ORDER_FILE = "user_order.dat";

    String ITEM_HASH_FILE = "item_hash.dat";

}
