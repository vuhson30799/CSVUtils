package main;

import main.entity.Constant;
import main.entity.Country;
import main.entity.LocationType;
import main.exeption.InputArgumentException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CSVFileUtils {
    private static List<String> headersInResultFile = new ArrayList<>();

    /**
     * This method gets two specified files (csv extension) to generate result file.
     * It will calculate new field names ROI.
     * @param pathToFolder path that links to the folder contains two needed files.
     * @param pathToResultFile path that links to where user wants to put the result file.
     * @param rate the current exchange rate from USD to VND.
     */
    public static void generateResultFile(String pathToFolder, String pathToResultFile, Integer rate) {
        File sourceFolder = new File(pathToFolder);
        if (!sourceFolder.isDirectory()) {
            throw new InputArgumentException("The input file must be folder.");
        }
        File[] files = sourceFolder.listFiles();
        if (files == null) {
            throw new InputArgumentException("The input folder must have files.");
        }
        if (files.length > 2) {
            throw new InputArgumentException("This folder contains more than 2 needed files.");
        }
        File destinationFile = new File(pathToResultFile);
        if (destinationFile.exists()) {
            throw new InputArgumentException("This file is exist in your system. Please choose the available path.");
        }
        List<Country> countryList = readInformationFromCSVFile(files);
        calculateROIData(countryList, rate);
        calculateBreakEventPoint(countryList, rate);
        exportCountryListAsCSVFile(countryList, destinationFile);
    }

    private static List<Country> readInformationFromCSVFile(File[] files) {
        List<Country> result = new ArrayList<>();

        Arrays.stream(files).forEach(csvFile -> {
            try {
                List<String> linesInFile = FileUtils.readLines(csvFile, Charset.defaultCharset());
                if (linesInFile.isEmpty()) {
                    throw new InputArgumentException("File is empty.");
                }

                String header = linesInFile.get(0);
                String[] fields = header.split(",");
                linesInFile.remove(0);
                headersInResultFile.addAll(Arrays.asList(fields));

                linesInFile.forEach(line -> {
                    String[] values = line.split(",");
                    for (int index = 0; index < values.length; index++) {
                        if (values[index].startsWith("\"") && values[index + 1].endsWith("\"")) {
                            values[index] = values[index].concat(",".concat(values[index + 1]));
                            values = reduceValuesArray(values, index + 1);
                        }
                    }

                    if (values.length != fields.length) {
                        throw new InputArgumentException("Value is missing for some fields.");
                    }
                    Country country = new Country();
                    country.setName(values[0]);
                    Map<String, String> calculatedData = new HashMap<>();
                    for (int index = 1; index < fields.length; index++) {
                        calculatedData.put(fields[index], values[index]);
                    }

                    if (!LocationType.AREA_OF_INTEREST.getValue().equals(calculatedData.get(Constant.LOCATION_FIELD))) {
                        country.setCalculatedData(calculatedData);
                        if (result.contains(country)) {
                            Country existCountry = result.get(result.indexOf(country));
                            existCountry.setCalculatedData(combineTwoCalculatedMap(existCountry.getCalculatedData(), country.getCalculatedData()));
                        } else {
                            result.add(country);
                        }
                    }

                });
            } catch (IOException e) {
                throw new InputArgumentException("Error while reading files.");
            }
        });
        return result;
    }

    private static Map<String, String> combineTwoCalculatedMap(Map<String, String> map1, Map<String, String> map2) {
        try{
            return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value1));
        }catch (Exception e) {
            throw new InputArgumentException("Input file includes records that duplicated.");
        }
    }

    private static void exportCountryListAsCSVFile(List<Country> countryList, File file) {
        try {
            if (file.createNewFile()) {
                List<String> lines = new ArrayList<>();
                lines.add(String.join(",", headersInResultFile));
                for (Country country : countryList) {
                    String line = country.getName();
                    for (String header : headersInResultFile) {

                        if (!header.equals("Country")) {
                            line = String.join(",", line, country.getCalculatedData().get(header) != null ? country.getCalculatedData().get(header) : "");
                        }
                    }
                    lines.add(line);
                }
                FileUtils.writeLines(file, lines);
            }
        } catch (IOException e) {
            throw new InputArgumentException("Directory is not exist.");
        }
    }

    private static void calculateROIData(List<Country> countryList, Integer rate) {
        if (rate == null) {
            rate = Constant.DEFAULT_RATE;
        }
        headersInResultFile.add(Constant.ROI_FIELD);
        for (Country country : countryList) {
            try {
                float estimatedEarning = Float.parseFloat(country.getCalculatedData().get(Constant.ESTIMATED_EARNING_FIELD));
                float costInVND = Float.parseFloat(country.getCalculatedData().get(Constant.COST_IN_VND));
                float roi = (estimatedEarning * rate - costInVND) / costInVND;
                country.getCalculatedData().put(Constant.ROI_FIELD, String.valueOf(roi));
            } catch (Exception e) {
                country.getCalculatedData().put(Constant.ROI_FIELD, "");
            }


        }
    }

    private static void calculateBreakEventPoint(List<Country> countryList, Integer rate) {
        if (rate == null) {
            rate = Constant.DEFAULT_RATE;
        }
        headersInResultFile.add(Constant.BREAK_EVENT_POINT_FIELD);
        for (Country country : countryList) {
            try {
                float estimatedEarning = Float.parseFloat(country.getCalculatedData().get(Constant.ESTIMATED_EARNING_FIELD));
                float conversions = Float.parseFloat(country.getCalculatedData().get(Constant.CONVERSIONS));
                float breakEventPoint = estimatedEarning * rate / conversions;
                country.getCalculatedData().put(Constant.BREAK_EVENT_POINT_FIELD, String.valueOf(breakEventPoint));
            }catch (Exception e) {
                country.getCalculatedData().put(Constant.BREAK_EVENT_POINT_FIELD, "");
            }
        }
    }

    private static String[] reduceValuesArray(String[] values, int index) {
        String[] result = new String[values.length - 1];
        for (int i = 0; i < values.length - 1; i++) {
            if (i >= index) {
                result[i] = values[i + 1];
            } else {
                result[i] = values[i];
            }
        }
        return result;
    }
}
