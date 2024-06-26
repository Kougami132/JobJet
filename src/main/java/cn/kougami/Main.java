package cn.kougami;

import cn.kougami.platinum.Zhaopin;
import cn.kougami.platinum.Zhipin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static String tag = "Java 实习";

    public static void main(String[] args) {
        List<String> blackList = Stream.of("阿里", "腾讯", "网易", "华为", "北大", "清华", "浙大", "大学", "研究院",
                "蚂蚁", "饿了么", "字节", "快手", "拼多多", "菜鸟", "哈啰", "新浪", "用友", "25届", "高级", "资深", "1-3年", "3-5年",
                "国腾工程顾问", "卓羽信息").collect(Collectors.toList());

        Zhipin boss = new Zhipin(tag, "101210100", blackList);
        boss.start();
        Zhaopin zhaopin = new Zhaopin(tag, "653", blackList);
        zhaopin.start();
    }
}