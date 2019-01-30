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
        //要idea的 vm option上加-D参数才有效,可能是idea的bug
        //System.out.println(System.getProperty("path"));

        if ( args.length <= 0 ){
            throw new RuntimeException("选项mockdata run或result未给,请至少给出一个");
        }

        String option = args[0];

        switch (option) {
            case "mockdata":
                Mock.mock(Paths.get(System.getProperty("path")));
                break;
            case "run":
                groupByAndSum(Paths.get(System.getProperty("user")),
                        Paths.get(System.getProperty("item")),
                        Paths.get(System.getProperty("out")),
                        Paths.get(System.getProperty("temp")), 16);
                break;
            case "show":
                Decode.show(Paths.get(System.getProperty("path")));
                break;
            default:
                throw new RuntimeException("不支持的命令");
        }


    }


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