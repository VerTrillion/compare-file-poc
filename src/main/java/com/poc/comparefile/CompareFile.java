package com.poc.comparefile;

import com.poc.comparefile.enums.CompareDirection;
import com.poc.comparefile.models.Record;
import com.poc.comparefile.models.DiffRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CompareFile {
    static List<Record> onlyLeftList = new ArrayList<>();
    static List<Record> onlyRightList = new ArrayList<>();
    static Map<String, DiffRecord> diffRecordList = new HashMap<>();

    public static void main(String[] args) {
        Instant start = Instant.now();
        if (args.length != 2) {
            System.out.println("Please input file path as arguments");
            return;
        }

        String leftFilePath = args[0];
        String rightFilePath = args[1];

        Map<String, Record> leftMap = getRecordList(leftFilePath);
        Map<String, Record> rightMap = getRecordList(rightFilePath);

        compare(leftMap, rightMap, CompareDirection.LEFT);
        compare(rightMap, leftMap, CompareDirection.RIGHT);

        System.out.println(onlyLeftList);
        System.out.println(onlyRightList);
        System.out.println(diffRecordList);

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println(timeElapsed.toMillis());
    }

    private static void compare(Map<String, Record> list1, Map<String, Record> list2, CompareDirection compareDirection) {
        for (String k : list1.keySet()) {
            Record record1 = list1.get(k);
            Record record2 = list2.get(k);
            if (record2 != null) {
                if (!record1.equals(record2)) {
                    DiffRecord diffRecord = new DiffRecord();
                    if (compareDirection == CompareDirection.LEFT) {
                        diffRecord.setLeftRecord(record1);
                        diffRecord.setRightRecord(record2);
                    } else {
                        diffRecord.setLeftRecord(record2);
                        diffRecord.setRightRecord(record1);
                    }
                    diffRecordList.putIfAbsent(k, diffRecord);
                }
            } else {
                if (compareDirection == CompareDirection.LEFT) {
                    onlyLeftList.add(record1);
                } else {
                    onlyRightList.add(record1);
                }
            }
        }
    }

    private static Map<String, Record> getRecordList(String filePath) {
        Map<String, Record> list = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(i -> {
                if (i != null) {
                    String[] cols = i.split("\\|");
                    if (cols.length == 3) {
                        Record record = new Record();
                        record.setCol1(cols[0]);
                        record.setCol2(cols[1]);
                        record.setCol3(cols[2]);
                        list.put(cols[0], record);
                    }
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
