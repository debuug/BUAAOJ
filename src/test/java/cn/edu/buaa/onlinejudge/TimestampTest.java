package cn.edu.buaa.onlinejudge;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;

public class TimestampTest extends OnlinejudgeApplicationTests {
    @Test
    public void testTimestamp(){
        //当前时间
        Timestamp current = new Timestamp(new Date().getTime());
        System.out.println("current = " + current.toString());
    }
}