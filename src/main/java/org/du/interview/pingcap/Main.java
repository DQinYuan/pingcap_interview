package org.du.interview.pingcap;

import org.du.interview.pingcap.hash.EHash;
import org.du.interview.pingcap.hash.HashFile;
import org.du.interview.pingcap.mock.Mock;
import org.du.interview.pingcap.sort.ESort;
import org.du.interview.pingcap.sum.ESum;
import org.du.interview.pingcap.util.Decode;
import org.du.interview.pingcap.util.EPathUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        //vm option上加-D参数才有效
        //System.out.println(System.getProperty("path"));

        if ( args.length <= 0 ){
            throw new RuntimeException("选项mockdata,run或result未给,请至少给出一个");
        }

        String option = args[0];

        switch (option) {
            case "mockdata":
                Mock.mock(Paths.get(System.getProperty("path")),
                        Integer.parseInt(System.getProperty("num")));
                break;
            case "run":
                long begin = System.currentTimeMillis();
                groupByAndSum(Paths.get(System.getProperty("user")),
                        Paths.get(System.getProperty("item")),
                        Paths.get(System.getProperty("out")),
                        Paths.get(System.getProperty("temp")), 16);
                System.out.println("time used:" + (System.currentTimeMillis() - begin) + " ms");
                break;
            case "show":
                Decode.show(Paths.get(System.getProperty("path")));
                break;
            default:
                throw new RuntimeException("不支持的命令");
        }


    }


    /**
     *
     * @param user user表数据文件的位置
     * @param item item表数据文件的位置
     * @param out  结果文件的摆放路径
     * @param tempDir 用于存放过程中产生的临时文件的文件夹路径
     * @param recordLen   表中每条记录的长度
     */
    public static void groupByAndSum(Path user, Path item, Path out,
                                     Path tempDir, int recordLen) {

        EPathUtil.createIfNotExist(tempDir);

        Path userOrderPath = tempDir.resolve(Config.USER_ORDER_FILE);

        //外部排序
        ESort.sort(user,
                userOrderPath,
                tempDir,
                recordLen, (l1, l2) -> Long.compare(l1, l2));

        //将item表按照item_id进行散列
        HashFile hashFile = EHash.hash(item, tempDir.resolve(Config.ITEM_HASH_FILE),
                recordLen);

        //按照user_id分组进行求和
        ESum.sum(userOrderPath, hashFile, out, recordLen);
    }

}
