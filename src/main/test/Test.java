package main.test;

import main.CSVFileUtils;
import main.entity.Constant;
import main.exeption.InputArgumentException;

public class Test {
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new InputArgumentException("Missing arguments.");
        }
        try {
            CSVFileUtils.generateResultFile(args[0], args[1], Integer.parseInt(args[2]));
        }catch (Exception e) {
            CSVFileUtils.generateResultFile(args[0], args[1], Constant.DEFAULT_RATE);
        }

    }
}
