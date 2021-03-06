package com.technology.flink.operator;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.concurrent.TimeUnit;

/**
 * @description: reduce是归并操作，它可以将KeyedStream 转变为 DataStream
 * 这里利用reduce函数实现了与sum函数相同的效果
 * @author: xzq
 * @date: 2021/2/19
 * @version:
 */
public class FoldJob {

    public static void main(String[] args) throws Exception {
        // 获取执行环境配置信息
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 连接socket获取输入的数据
        DataStream<String> textStream = env.socketTextStream("127.0.0.1", 9000);

        //3.
        DataStream<String> result = textStream
                //map是将每一行单词变为一个tuple2
                .map(line -> Tuple2.of(line.trim(), 1))
                //如果要用Lambda表示是，Tuple2是泛型，那就得用returns指定类型。
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                //keyBy进行分区，按照第一列，也就是按照单词进行分区
                .keyBy(0)
                //指定窗口，每10秒个计算一次
                .timeWindow(Time.of(10, TimeUnit.SECONDS))
                //指定一个开始的值，对每一组内的元素进行归并操作，即第一个和第二个归并，结果再与第三个归并...
                .fold("结果：",(String current, Tuple2<String, Integer> t2) -> current+t2.f0+",");


        //4.打印输出sink
        result.print();
        //5.开始执行
        env.execute();
    }

}
