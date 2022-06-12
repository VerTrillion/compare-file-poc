package com.poc.comparefile;

import com.poc.comparefile.enums.CompareDirection;
import com.poc.comparefile.enums.CompareField;
import com.poc.comparefile.models.DiffField;
import com.poc.comparefile.models.DiffRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
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
    static List<String> onlyLeftList = new ArrayList<>();
    static List<String> onlyRightList = new ArrayList<>();
    static Map<String, DiffRecord> diffRecordList = new HashMap<>();
    static Map<String, List<DiffField>> diffDetailList = new HashMap<>();
    static String outputDirectory;
    static Logger logger = LogManager.getLogger(CompareFile.class);

    static final int LOG_PRINTING_INTERVAL = 1000;

    static int count = 1;

    public static void main(String[] args) {
        Instant start = Instant.now();
        if (args.length != 3) {
            logger.info("Please input 3 arguments");
            return;
        }

        String leftFilePath = args[0];
        String rightFilePath = args[1];
        outputDirectory = args[2];

        try {
            Map<String, String> leftMap = getRecordList(leftFilePath);
            Map<String, String> rightMap = getRecordList(rightFilePath);

            compare(leftMap, rightMap, CompareDirection.LEFT);
            compare(rightMap, leftMap, CompareDirection.RIGHT);
            getDiffDetail();

            writeOnlySideToFile(onlyLeftList, CompareDirection.LEFT);
            writeOnlySideToFile(onlyRightList, CompareDirection.RIGHT);
            writeDiffRecordToFile(diffRecordList);
            writeDiffDetailToFile(diffDetailList);

            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            logger.info("elapsed: {} ms", timeElapsed.toMillis());
        } catch (Exception e) {
            logger.error("compare file errors:", e);
        }
    }



    private static void compare(Map<String, String> list1, Map<String, String> list2, CompareDirection compareDirection) {
        logger.info("## Start comparing in {} direction", compareDirection.toString());
        count = 1;
        for (Map.Entry<String, String> entry : list1.entrySet()) {
            String record1 = entry.getValue();
            String record2 = list2.get(entry.getKey());
            if (record2 != null) {
                handleDifferentRecord(record1, record2, compareDirection, entry.getKey());
            } else {
                handleOnlySideRecord(record1, compareDirection);
            }
            if (count % LOG_PRINTING_INTERVAL == 0) {
                logger.info("compared {}/{} record(s) in {} direction", count, list1.size(), compareDirection.toString());
            }
            count++;
        }
        logger.info("## Done comparing in {} direction", compareDirection.toString());
    }

    private static void handleDifferentRecord(String record1, String record2, CompareDirection compareDirection, String key) {
        if (!record1.equals(record2)) {
            DiffRecord diffRecord = new DiffRecord();
            if (compareDirection == CompareDirection.LEFT) {
                diffRecord.setLeftRecord(record1);
                diffRecord.setRightRecord(record2);
            } else {
                diffRecord.setLeftRecord(record2);
                diffRecord.setRightRecord(record1);
            }
            diffRecordList.putIfAbsent(key, diffRecord);
        }
    }

    private static void handleOnlySideRecord(String record1, CompareDirection compareDirection) {
        if (compareDirection == CompareDirection.LEFT) {
            onlyLeftList.add(record1);
        } else {
            onlyRightList.add(record1);
        }
    }


    private static Map<String, String> getRecordList(String filePath) throws IOException {
        logger.info("## Start reading {} file", filePath);
        Map<String, String> list = new HashMap<>();
        count = 1;
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(line -> {
                String[] cols = line.split("\\|");
                if (cols.length > 2) {
                    list.put(cols[0], line);
                }
                if (count % LOG_PRINTING_INTERVAL == 0) {
                    logger.info("read {} line(s) in of {} file", count, filePath);
                }
                count++;
            });
        } catch (Exception e) {
            logger.error("getRecordList error: ", e);
            throw e;
        }
        logger.info("## Done reading {} file", filePath);
        return list;
    }

    private static void getDiffDetail() {
        logger.info("## Start getting diff detail");
        count = 1;
        for (Map.Entry<String, DiffRecord> entry : diffRecordList.entrySet()) {
            String key = entry.getKey();
            DiffRecord diff = entry.getValue();

            String leftDiff = diff.getLeftRecord();
            String rightDiff = diff.getRightRecord();

            String[] leftValList = leftDiff.split("\\|");
            String[] rightValList = rightDiff.split("\\|");

            List<DiffField> diffFields = new ArrayList<>();

            if (leftValList.length == rightValList.length) {
                for (int i = 0; i < leftValList.length; i++) {
                    String leftVal =  leftValList[i];
                    String rightVal =  leftValList[i];

                    if (!leftVal.equals(rightVal)) {
                        String fieldName = CompareField.FIELD_NAME[i];

                        DiffField diffField = new DiffField();
                        diffField.setFieldName(fieldName);
                        diffField.setLeftValue(leftVal);
                        diffField.setRightValue(rightVal);

                        diffFields.add(diffField);
                    }
                }
            }
            diffDetailList.put(key, diffFields);

            if (count % LOG_PRINTING_INTERVAL == 0) {
                logger.info("got details of {}/{} record(s)", count, diffRecordList.size());
            }
            count++;
        }
        logger.info("## Done getting diff detail");
    }

    private static String getOutputDirectory() {
        if (outputDirectory.endsWith("/"))
            return outputDirectory;
        else
            return outputDirectory + "/";
    }

    private static void writeOnlySideToFile(List<String> onlySideList, CompareDirection direction) throws Exception {
        File file = new File(getOutputDirectory() + direction.toString().toLowerCase() + "Only.txt");
        file.getParentFile().mkdirs();

        logger.info("## Start writing {} file", file.getName());
        count = 1;

        try (FileWriter writer = new FileWriter(file)) {
            for (String line: onlySideList) {
                writeLine(writer, line);

                if (count % LOG_PRINTING_INTERVAL == 0) {
                    logger.info("wrote {}/{} line(s) of {} file", count, onlySideList.size(), file.getName());
                }
                count++;
            }
        } catch (Exception e) {
            logger.error("writeOnlySideToFile error: ", e);
            throw e;
        }
        logger.info("## Done writing {} file", file.getName());
    }

    private static void writeDiffRecordToFile(Map<String, DiffRecord> diffRecordList) throws Exception {
        File file = new File(getOutputDirectory() + "diff.txt");
        file.getParentFile().mkdirs();

        logger.info("## Start writing {} file", file.getName());
        count = 1;

        try (FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<String, DiffRecord> entry : diffRecordList.entrySet()) {
                DiffRecord diff = entry.getValue();
                String left = diff.getLeftRecord();
                String right = diff.getRightRecord();

                writeLine(writer, "left:" + left);
                writeLine(writer, "right:" + right);
                writeLine(writer, "------------------------------------------------");

                if (count % LOG_PRINTING_INTERVAL == 0) {
                    logger.info("wrote {}/{} line(s) of {} file", count, diffRecordList.size(), file.getName());
                }
                count++;
            }
        } catch (Exception e) {
            logger.error("writeDiffRecordToFile error: ", e);
            throw e;
        }
        logger.info("## Done writing {} file", file.getName());
    }

    private static void writeDiffDetailToFile(Map<String, List<DiffField>> diffDetailList) throws IOException {
        File file = new File(getOutputDirectory() + "diffDetail.txt");
        file.getParentFile().mkdirs();

        logger.info("## Start writing {} file", file.getName());
        count = 1;

        try (FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<String, List<DiffField>> entry : diffDetailList.entrySet()) {
                String key = entry.getKey();
                writeLine(writer, "## " + key);
                List<DiffField> diffFields = entry.getValue();
                for (DiffField diffField : diffFields) {
                    writeLine(writer, String.format("field=%s|left=%s|right=%s", diffField.getFieldName(), diffField.getLeftValue(), diffField.getRightValue()));
                }
                writeLine(writer, "------------------------------------------------");

                if (count % LOG_PRINTING_INTERVAL == 0) {
                    logger.info("wrote {}/{} line(s) of {} file", count, diffRecordList.size(), file.getName());
                }
                count++;
            }
        } catch (Exception e) {
            logger.error("writeDiffDetailToFile error: ", e);
            throw e;
        }
        logger.info("## Done writing {} file", file.getName());
    }

    private static void writeLine(FileWriter writer, String line) throws IOException {
        writer.write(line + System.lineSeparator());
    }
}
