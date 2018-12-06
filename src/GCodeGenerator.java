import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class GCodeGenerator {
    private static Double lengthUsed;
    private static Double layerHeight;
    private static Integer scaling;
    private static Double filamentDiameter;
    private static Integer fileLine;
    private static ArrayList<String> zStrings = new ArrayList<>();
    private static Double maxDim;
    private static Boolean isRetract = false;

    private static ArrayList<Double> zPrintList = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();
    private static ArrayList<Point> infillBoundPointList = new ArrayList<>();
    private static ArrayList<Line> infillLineList = new ArrayList<>();
    private static ArrayList<Point> pointTempList = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> shellList = new ArrayList<>();

    private static Double firstBedTemperature;
    private static Double firstExtruderTemperature;
    private static Double bedTemperature;
    private static Double extruderTemperature;
    private static Double travelSpeedValue;
    private static Double retractSpeed;
    private static Double retractLengthValue;
    private static Double firstLayerSpeed;
    private static Integer shellLayers;
    private static Double avgX, avgY;
    private static Double extruderWidth;
    private static Point previousPrintPoint;
    private static Double infillLineAngle;
    private static Integer bottomLayers, innerLayers, topLayers;
    private static Double lineGap;
    private static Double shellSpeed, externalShellSpeed;
    private static Double solidInfillSpeed, infillSpeed;
    private static Double lastTopSpeed;

    public static void generator() {
        maxDim = 100.0;
        storeStartEndGcode();
        reconstructFile();
        storeData();
        String code = "";
        code = generateStart(code);
        code = generateBasic(code);
        code = generateBottom(code);
        code = generateInner(code);
        code = generateTop(code);
        code = generateLastTop(code);
        code = generateEnd(code);
        try {
            FileWriter fileWriter = new FileWriter(new File("File/Output.gcode"));
            fileWriter.write(code);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Properties properties = new Properties();
//        try {
//            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
//            properties.getProperty("");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static void storeStartEndGcode() {
        try {
            FileWriter writerStart = new FileWriter(new File("File/startGcode.txt"));
            FileWriter writerEnd = new FileWriter(new File("File/endGcode.txt"));
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            String startGcode = properties.getProperty("startGcode");
            String endGcode = properties.getProperty("endGcode");
            writerStart.write(startGcode);
            writerEnd.write(endGcode);
            writerStart.flush();
            writerEnd.flush();
            writerStart.close();
            writerEnd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void reconstructFile() {
        try {
            FileInputStream zFile = new FileInputStream("File/zListFile.txt");
            BufferedReader readerZ = new BufferedReader(new InputStreamReader(zFile));
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            Double height = Double.parseDouble(properties.getProperty("height"));
            Double firstHeight = Double.parseDouble(properties.getProperty("firstHeight"));
            Integer count = 0;
            Double zHeight = 0.0;
            String line = readerZ.readLine();
            while (line != null) {
                String[] data = line.split(" ");
                if (data[0].equals("")) break;
                if (count == 0 && Double.parseDouble(data[0]) != 0) {
                    readerZ.close();
                    return;
                }
                zHeight = Double.parseDouble(data[0]);
                if (count == 0) {
                    zHeight += firstHeight;
                } else zHeight += height;
                String newLine = "";
                Integer length = data.length;
                for (int i = 0; i < length; i++) {
                    newLine += (String.valueOf(zHeight) + " ");
                }
                zStrings.add(newLine);
                count++;
                line = readerZ.readLine();
            }
            readerZ.close();
            File fileZ = new File("File/zListFile.txt");
            FileWriter fileWriter = new FileWriter(fileZ);
            fileWriter.write("");
            for (String fileString : zStrings) {
                fileWriter.write(fileString);
                fileWriter.write("\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void storeData() {
        try {
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            scaling = Integer.parseInt(properties.getProperty("scaling"));
            FileInputStream fileX = new FileInputStream("File/xListFile.txt");
            FileInputStream fileY = new FileInputStream("File/yListFile.txt");
            FileInputStream fileZ = new FileInputStream("File/zListFile.txt");
            BufferedReader readerX = new BufferedReader(new InputStreamReader(fileX));
            BufferedReader readerY = new BufferedReader(new InputStreamReader(fileY));
            BufferedReader readerZ = new BufferedReader(new InputStreamReader(fileZ));
            String lineX = readerX.readLine();
            String lineY = readerY.readLine();
            String lineZ = readerZ.readLine();
            while ((lineX != null) && (lineY != null) && (lineZ != null)) {
                Double zData = Double.parseDouble(lineZ.split(" ")[0]);
                zPrintList.add(zData * scaling);
                String[] xPoints = lineX.split(" ");
                String[] yPoints = lineY.split(" ");
                ArrayList<Point> singleLayerPoints = new ArrayList<>();
                for (int i = 0; i < xPoints.length; i++) {
                    Point point = new Point(Double.parseDouble(xPoints[i]) * scaling,
                            Double.parseDouble(yPoints[i]) * scaling);
//                    point.setX(Double.parseDouble(xPoints[i]));
//                    point.setY(Double.parseDouble(yPoints[i]));
                    singleLayerPoints.add(point);
                }
                singleLayerPoints.remove(singleLayerPoints.size() - 1);
                pointPrintList.add(singleLayerPoints);
                lineX = readerX.readLine();
                lineY = readerY.readLine();
                lineZ = readerZ.readLine();
            }
            readerX.close();
            readerY.close();
            readerZ.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateStart(String code) {
        String gcode = code;
        gcode += "M107\n";
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            String firstBedTemp = properties.getProperty("firstBedTemp");
            firstBedTemperature = Double.parseDouble(firstBedTemp);
            gcode += ("M190 S" + formatNumber(firstBedTemperature, 0) + "\n");
            String firstExtruderTemp = properties.getProperty("firstExtruderTemp");
            firstExtruderTemperature = Double.parseDouble(firstExtruderTemp);
            gcode += ("M104 S" + formatNumber(firstExtruderTemperature, 0) + "\n");
            FileInputStream fileStart = new FileInputStream("File/startGcode.txt");
            BufferedReader readerStart = new BufferedReader(new InputStreamReader(fileStart));
            String startGcode = "";
            String line = readerStart.readLine();
            while (line != null) {
                line = line.trim();
                startGcode += (line + "\n");
                line = readerStart.readLine();
            }
            gcode += startGcode;
//            gcode += "\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateBasic(String code) {
        String gcode = code;
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            gcode += ("M109 S" + properties.getProperty("firstExtruderTemp") + "\n");
            gcode += "G21\nG90\nG82\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateBottom(String code) {
        String gcode = code;
        gcode += "G92 E0\n";
        Properties properties = new Properties();
        try {
            layerHeight = 0.0;
            fileLine = 0;
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            bedTemperature = Double.parseDouble(properties.getProperty("bedTemp"));
            extruderTemperature = Double.parseDouble(properties.getProperty("extruderTemp"));
            bottomLayers = Integer.parseInt(properties.getProperty("bottomLayers"));
            shellSpeed = Double.parseDouble(properties.getProperty("shellSpeed")) * 60;
            externalShellSpeed = Double.parseDouble(properties.getProperty("externalShellSpeed")) * 60;
            solidInfillSpeed = Double.parseDouble(properties.getProperty("solidInfillSpeed")) * 60;
            String travelSpeed = properties.getProperty("travelSpeed");
            String firstHeight = properties.getProperty("firstHeight");
            travelSpeedValue = Double.parseDouble(travelSpeed) * 60;
            double height = zPrintList.get(fileLine);
            gcode += "G1 Z" + formatNumber(height, 3)
                    + " F" + formatNumber(travelSpeedValue, 3) + "\n";
            String retractLength = properties.getProperty("retractLength");
            retractLengthValue = Double.parseDouble(retractLength);
            retractSpeed = Double.parseDouble(properties.getProperty("retractSpeed")) * 60;
            gcode += "G1 E-" + formatNumber(retractLengthValue, 5)
                    + " F" + formatNumber(retractSpeed, 5) + "\n";
            gcode += "G92 E0\n";
            lengthUsed = -1 * retractLengthValue;
            gcode = generateFirstBottom(gcode, Math.PI / 4);
            gcode += "M104 S" + formatNumber(extruderTemperature, 0) + "\n";
            gcode += "M140 S" + formatNumber(bedTemperature, 0) + "\n";
            gcode = generateBottomTop(gcode, bottomLayers);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateFirstBottom(String code, Double lineAngle) {
        String gcode = code;
        layerHeight = zPrintList.get(fileLine);
        ArrayList<Point> pointList = pointPrintList.get(fileLine);
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            filamentDiameter = Double.parseDouble(properties.getProperty("filamentDiameter"));
            calculateAverage(pointList);
            shellLayers = Integer.parseInt(properties.getProperty("shellLayers"));
            extruderWidth = Double.parseDouble(properties.getProperty("extruderWidth"));
//            Point pointStart;
//            pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                    pointList.get(1), shellLayers, extruderWidth);
//            previousPrintPoint = pointStart;
//            gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
//                    + " Y" + formatNumber(pointStart.getY(), 3)
//                    + " F" + formatNumber(travelSpeedValue, 3)
//                    + "\n";
//            gcode += "G1 E" + formatNumber(retractLengthValue, 5)
//                    + " F" + formatNumber(retractSpeed, 5)
//                    + "\n";

            firstLayerSpeed = Double.parseDouble(properties.getProperty("firstLayerSpeed")) * 60;

//            lengthUsed = retractLengthValue;
            pointTempList = pointList;
            shellList.add(pointTempList);
            for (int i = 0; i < shellLayers - 1; i++) {
                generateEachShell(gcode, i, firstLayerSpeed);
            }
            isRetract = true;
            gcode = generateEachShellCode(gcode, 0, firstLayerSpeed);


            gcode = generateSolidInfill(pointList, gcode, lineAngle, firstLayerSpeed, extruderWidth * scaling, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileLine++;
        return gcode;
    }

    private static void generateEachShell(String code, Integer index,
                                          Double shellSpeed) {
//        pointTempList.clear();
        Boolean testDelete = false;
        ArrayList<Point> pointStoreList = new ArrayList<>();
        Point printPoint;
        for (int i = 1; i < pointTempList.size(); i++) {
            if (i == pointTempList.size() - 1) {
                printPoint = layerAlgorithm(pointTempList.get(i - 1), pointTempList.get(i),
                        pointTempList.get(0), 2, extruderWidth);
            } else {
                printPoint = layerAlgorithm(pointTempList.get(i - 1), pointTempList.get(i),
                        pointTempList.get(i + 1), 2, extruderWidth);
            }
//            if (printPoint.getY() > 100 || printPoint.getY() < 0) {
//                continue;
//            }
            for (int j = 0; j < pointStoreList.size(); j++) {
                if (calculatePointDistance(printPoint, pointStoreList.get(j)) <= extruderWidth / 20) {
                    testDelete = true;
                    break;
                }
            }
            if (!testDelete){
                pointStoreList.add(printPoint);
            }
            testDelete = false;
        }
        printPoint = layerAlgorithm(pointTempList.get(pointTempList.size() - 1), pointTempList.get(0),
                pointTempList.get(1), 2, extruderWidth);
        for (int j = 0; j < pointStoreList.size(); j++) {
            if (calculatePointDistance(printPoint, pointStoreList.get(j)) <= extruderWidth / 20) {
                testDelete = true;
                break;
            }
        }
        if (!testDelete) {
            pointStoreList.add(printPoint);
        }
        testDelete = false;
        pointTempList = pointStoreList;
        shellList.add(pointTempList);
    }

    private static String generateEachShellCode(String code, Integer index,
                                                Double shellSpeed) {
        String gcode = code;
        Point pointStart;
        pointStart = shellList.get(shellList.size() - 1).get(0);
        previousPrintPoint = pointStart;
        gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                + " Y" + formatNumber(pointStart.getY(), 3)
                + " F" + formatNumber(travelSpeedValue, 3)
                + "\n";
        if (isRetract) {
            gcode += "G1 E" + formatNumber(retractLengthValue, 5)
                    + " F" + formatNumber(retractSpeed, 5)
                    + "\n";
            lengthUsed = retractLengthValue;
            isRetract = false;
        }

        Point printPoint;
        Double extrusion;
        for (int i = shellList.size() - 1; i >= 0; i--) {
            ArrayList<Point> pointStoreList = shellList.get(i);
            if (i != shellList.size() - 1){
                pointStart = pointStoreList.get(0);
                gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                        + " Y" + formatNumber(pointStart.getY(), 3)
                        + " F" + formatNumber(travelSpeedValue, 3)
                        + "\n";
                previousPrintPoint = pointStart;
            }
            gcode += "G1 F" + formatNumber(shellSpeed, 0) + "\n";
            for (int j = 1; j < pointStoreList.size(); j++) {
                printPoint = pointStoreList.get(j);
                extrusion = extrusionData(previousPrintPoint, printPoint);
                lengthUsed += extrusion;
                if (printPoint.getY() < 0) {
                    int temp = 0;
                }
                gcode += "G1 X" + formatNumber(printPoint.getX(), 3)
                        + " Y" + formatNumber(printPoint.getY(), 3)
                        + " E" + formatNumber(lengthUsed, 5)
                        + "\n";
                previousPrintPoint = printPoint;
            }
        }
        return gcode;
        //        gcode += "G1 F" + formatNumber(shellSpeed, 0) + "\n";
//        Integer layer = shellLayers - index;
//        Point printPoint;
//        Double extrusion;
//        for (int i = 1; i < pointList.size(); i++) {
//            if (i == pointList.size() - 1) {
//                printPoint = layerAlgorithm(pointList.get(i - 1), pointList.get(i),
//                        pointList.get(0), layer, extruderWidth);
//            } else {
//                printPoint = layerAlgorithm(pointList.get(i - 1), pointList.get(i),
//                        pointList.get(i + 1), layer, extruderWidth);
//            }
//            extrusion = extrusionData(previousPrintPoint, printPoint);
//            lengthUsed += exctrusion;
//            gcode += "G1 X" + formatNumber(printPoint.getX(), 3)
//                    + " Y" + formatNumber(printPoint.getY(), 3)
//                    + " E" + formatNumber(lengthUsed, 5)
//                    + "\n";
//            pointTempList.add(printPoint);
//            previousPrintPoint = printPoint;
//        }
//        printPoint = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                pointList.get(1), layer, extruderWidth);
//        extrusion = extrusionData(previousPrintPoint, printPoint);
//        lengthUsed += extrusion;
//        gcode += "G1 X" + formatNumber(printPoint.getX(), 3)
//                + " Y" + formatNumber(printPoint.getY(), 3)
//                + " E" + formatNumber(lengthUsed, 5)
//                + "\n";
//        pointTempList.add(printPoint);
//        Point pointNewStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                pointList.get(1), layer - 1, extruderWidth);
//        previousPrintPoint = pointNewStart;
//        gcode += "G1 X" + formatNumber(pointNewStart.getX(), 3)
//                + " Y" + formatNumber(pointNewStart.getY(), 3)
//                + " F" + formatNumber(travelSpeedValue, 3)
//                + "\n";
    }

    private static Double calculatePointDistance(Point pointFirst, Point pointSecond) {
        return Math.sqrt(Math.pow(pointFirst.getX() - pointSecond.getX(), 2) + Math.pow(pointFirst.getY() - pointSecond.getY(), 2));
    }

    //REMAIN Modification!
    private static Point layerAlgorithm(Point pointPrevious, Point point, Point pointLatter,
                                        Integer layer, Double d) {
        Double w1_1 = pointLatter.getY() - point.getY();
        Double w1_2 = point.getX() - pointLatter.getX();
        Double w2_1 = point.getY() - pointPrevious.getY();
        Double w2_2 = pointPrevious.getX() - point.getX();
        Double b_1 = pointLatter.getX() * point.getY() - point.getX() * pointLatter.getY();
        Double b_2 = point.getX() * pointPrevious.getY() - pointPrevious.getX() * point.getY();
        Double C_1 = 0.0, C_2 = 0.0;
        if (w1_1 * avgX + w1_2 * avgY + b_1 < 0) {
            C_1 = b_1 + (layer - 1) * d * Math.sqrt(Math.pow(w1_1, 2) + Math.pow(w1_2, 2));
        } else {
            C_1 = b_1 - (layer - 1) * d * Math.sqrt(Math.pow(w1_1, 2) + Math.pow(w1_2, 2));
        }
        if (w2_1 * avgX + w2_2 * avgY + b_2 < 0) {
            C_2 = b_2 + (layer - 1) * d * Math.sqrt(Math.pow(w2_1, 2) + Math.pow(w2_2, 2));
        } else {
            C_2 = b_2 - (layer - 1) * d * Math.sqrt(Math.pow(w2_1, 2) + Math.pow(w2_2, 2));
        }
        Double pointX, pointY;
        if (w2_2 * w1_1 - w2_1 * w1_2 != 0) {
            pointX = (w1_2 * C_2 - w2_2 * C_1) / (w2_2 * w1_1 - w2_1 * w1_2);
            pointY = (w1_1 * C_2 - w2_1 * C_1) / (w1_2 * w2_1 - w2_2 * w1_1);
        } else {
            Double w3_1 = w1_2;
            Double w3_2 = -1 * w1_1;
            Double C_3 = w1_1 * point.getY() - w1_2 * point.getX();
            pointX = (w1_2 * C_3 - w3_2 * C_1) / (w3_2 * w1_1 - w3_1 * w1_2);
            pointY = (w1_1 * C_3 - w3_1 * C_1) / (w1_2 * w3_1 - w3_2 * w1_1);
        }
        return new Point(pointX, pointY);
    }

    private static void calculateAverage(ArrayList<Point> pointList) {
        Double sumX = 0.0, sumY = 0.0;
        for (Point point : pointList) {
            sumX += point.getX();
            sumY += point.getY();
        }
        avgX = sumX / pointList.size();
        avgY = sumY / pointList.size();
    }

    private static String formatNumber(Double number, Integer prefix) {
        String numberResult;
        numberResult = String.format("%." + String.valueOf(prefix) + "f", number);
        return numberResult;
    }

    private static Double extrusionData(Point previous, Point point) {
        Double length = Math.sqrt(Math.pow(point.getX() - previous.getX(), 2) +
                Math.pow(point.getY() - previous.getY(), 2));
        Double volume = length * extruderWidth * layerHeight;
        Double area = (Math.PI * Math.pow(filamentDiameter, 2)) / 4;
        return volume / area;
    }

    private static String generateSolidInfill(ArrayList<Point> pointList, String code, Double lineAngle,
                                              Double solidInfillSpeed, Double gapOfInfillLine,
                                              Boolean ifRetraction) {
        infillLineList.clear();
        infillBoundPointList.clear();
        String gcode = code;
        Integer virtualLayer = shellLayers + 1;
//        gcode += "G1 F" + formatNumber(firstLayerSpeed, 0) + "\n";
        Point printPoint;
        for (int i = 1; i < pointList.size(); i++) {
            if (i == pointList.size() - 1) {
                printPoint = layerAlgorithm(pointList.get(i - 1), pointList.get(i),
                        pointList.get(0), virtualLayer, extruderWidth);
            } else {
                printPoint = layerAlgorithm(pointList.get(i - 1), pointList.get(i),
                        pointList.get(i + 1), virtualLayer, extruderWidth);
            }
            infillBoundPointList.add(printPoint);
        }
        printPoint = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                pointList.get(1), virtualLayer, extruderWidth);
        infillBoundPointList.add(printPoint);
        infillLineAngle = lineAngle;
        constructInfillLines(gapOfInfillLine);
        for (int i = 0; i < infillLineList.size(); i++) {
            if (infillLineList.get(i).getStartPoint().getX() <
                    infillLineList.get(i).getEndPoint().getX()) {
                Point pointStart = infillLineList.get(i).getStartPoint();
                Point pointEnd = infillLineList.get(i).getEndPoint();
                Point pointStartNew = new Point(pointEnd.getX(), pointEnd.getY());
                Point pointEndNew = new Point(pointStart.getX(), pointStart.getY());
                infillLineList.set(i, new Line(pointStartNew, pointEndNew));
            }
        }
        Line lineStart = infillLineList.get(0);
        Point pointStart = lineStart.getStartPoint();
        gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                + " Y" + formatNumber(pointStart.getY(), 3)
                + " F" + formatNumber(travelSpeedValue, 3)
                + "\n";
        previousPrintPoint = pointStart;
        if (ifRetraction) {
            gcode += "G1 E" + formatNumber(retractLengthValue, 5)
                    + " F" + formatNumber(retractSpeed, 5)
                    + "\n";
        }
        gcode += "G1 F" + formatNumber(solidInfillSpeed, 0) + "\n";
        Point pointEnd = lineStart.getEndPoint();
        Double extrusion = extrusionData(previousPrintPoint, pointEnd);
        lengthUsed += extrusion;
        gcode += "G1 X" + formatNumber(pointEnd.getX(), 3)
                + " Y" + formatNumber(pointEnd.getY(), 3)
                + " E" + formatNumber(lengthUsed, 5)
                + "\n";
        previousPrintPoint = pointEnd;
        for (int i = 1; i < infillLineList.size(); i++) {
            Line line = infillLineList.get(i);
            if (i % 2 == 1) {
                pointStart = line.getEndPoint();
                pointEnd = line.getStartPoint();
            } else {
                pointStart = line.getStartPoint();
                pointEnd = line.getEndPoint();
            }
            extrusion = extrusionData(previousPrintPoint, pointStart);
            lengthUsed += extrusion;
            gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                    + " Y" + formatNumber(pointStart.getY(), 3)
                    + " E" + formatNumber(lengthUsed, 5)
                    + "\n";
            previousPrintPoint = pointStart;
            extrusion = extrusionData(previousPrintPoint, pointEnd);
            lengthUsed += extrusion;
            gcode += "G1 X" + formatNumber(pointEnd.getX(), 3)
                    + " Y" + formatNumber(pointEnd.getY(), 3)
                    + " E" + formatNumber(lengthUsed, 5)
                    + "\n";
            previousPrintPoint = pointEnd;
        }
        return gcode;
    }

    private static void constructInfillLines(Double gapOfInfillLine) {
        Integer pointOrder = 0;
        Point pointStart = null, pointEnd = null;
        Double startM, endM;
        if (infillLineAngle < Math.PI / 2) {
            startM = -1 * Math.tan(infillLineAngle) * maxDim;
            endM = maxDim;
        } else {
            startM = 0.0;
            endM = maxDim - Math.tan(infillLineAngle) * maxDim;
        }
        for (Double m = startM; m <= endM; m += gapOfInfillLine) {
            Double value1, value2;
            Double k = Math.tan(infillLineAngle);
            for (int i = 0; i < infillBoundPointList.size() - 1; i++) {
                value1 = k * infillBoundPointList.get(i).getX()
                        - infillBoundPointList.get(i).getY() + m;
                value2 = k * infillBoundPointList.get(i + 1).getX()
                        - infillBoundPointList.get(i + 1).getY() + m;
                if (value1 * value2 < 0) {
                    if (pointOrder == 0) {
                        pointStart = calculateCrossPoint(k, m,
                                infillBoundPointList.get(i), infillBoundPointList.get(i + 1));
                        pointOrder++;
                    } else {
                        pointEnd = calculateCrossPoint(k, m,
                                infillBoundPointList.get(i), infillBoundPointList.get(i + 1));
                        if (Math.abs(pointEnd.getX() - pointStart.getX()) > 1) {
                            pointOrder++;
                        }
                    }
                    if (pointOrder == 2) {
                        Line line = new Line(pointStart, pointEnd);
                        infillLineList.add(line);
                        pointOrder = 0;
                        break;
                    }
                }
            }
            value1 = k * infillBoundPointList.get(infillBoundPointList.size() - 1).getX()
                    - infillBoundPointList.get(infillBoundPointList.size() - 1).getY() + m;
            value2 = k * infillBoundPointList.get(0).getX()
                    - infillBoundPointList.get(0).getY() + m;
            if (value1 * value2 < 0) {
                if (pointOrder == 0) {
                    pointStart = calculateCrossPoint(k, m,
                            infillBoundPointList.get(infillBoundPointList.size() - 1),
                            infillBoundPointList.get(0));
                    pointOrder++;
                } else {
                    pointEnd = calculateCrossPoint(k, m,
                            infillBoundPointList.get(infillBoundPointList.size() - 1),
                            infillBoundPointList.get(0));
                    pointOrder++;
                }
                if (pointOrder == 2) {
                    Line line = new Line(pointStart, pointEnd);
                    infillLineList.add(line);
                    if (Math.abs(pointEnd.getX() - pointStart.getX()) > 1) {
                        pointOrder = 0;
                    }
                }
            }
        }
    }

    private static Point calculateCrossPoint(Double k, Double m, Point point1, Point point2) {
        Double w1_2 = -1.0;
        Double w2_1 = point2.getY() - point1.getY();
        Double w2_2 = point1.getX() - point2.getX();
        Double C_2 = point2.getX() * point1.getY() - point1.getX() * point2.getY();
        Double pointX = (w1_2 * C_2 - w2_2 * m) / (w2_2 * k - w2_1 * w1_2);
        Double pointY = (k * C_2 - w2_1 * m) / (w1_2 * w2_1 - w2_2 * k);
        return new Point(pointX, pointY);
    }

    private static String generateBottomTop(String code, int layers) {
        String gcode = code;
        Double height;
        for (int i = 1; i <= layers; i++) {
            height = zPrintList.get(fileLine);
            gcode += "G1 Z" + formatNumber(height, 3)
                    + " F" + formatNumber(travelSpeedValue, 0) + "\n";
            ArrayList<Point> pointList = pointPrintList.get(fileLine);
            calculateAverage(pointList);
            Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                    pointList.get(1), shellLayers, extruderWidth);
            previousPrintPoint = pointStart;
            gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                    + " Y" + formatNumber(pointStart.getY(), 3)
                    + " F" + formatNumber(travelSpeedValue, 3)
                    + "\n";


            pointTempList = pointList;
            for (int j = 0; j < shellLayers - 1; j++) {
                generateEachShell(gcode, j, shellSpeed);
            }
            generateEachShell(gcode, shellLayers - 1, externalShellSpeed);


            if (i % 2 == 1)
                gcode = generateSolidInfill(pointList, gcode, 3 * Math.PI / 4, solidInfillSpeed,
                        extruderWidth * scaling, false);
            else gcode = generateSolidInfill(pointList, gcode, Math.PI / 4, solidInfillSpeed,
                    extruderWidth * scaling, false);
            fileLine++;
        }
        return gcode;
    }

    private static String generateInner(String code) {
        String gcode = code;
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            topLayers = Integer.parseInt(properties.getProperty("topLayers"));
            innerLayers = zPrintList.size() - topLayers - bottomLayers - 1;
            infillSpeed = Double.parseDouble(properties.getProperty("infillSpeed")) * 60;
            lineGap = Double.parseDouble(properties.getProperty("lineGap"));
//            gcode += "M106 S211.65\n";
            Double height;
            for (int i = 1; i <= innerLayers; i++) {
                height = zPrintList.get(fileLine);
                gcode += "G1 Z" + formatNumber(height, 3)
                        + " F" + formatNumber(travelSpeedValue, 0) + "\n";
                ArrayList<Point> pointList = pointPrintList.get(fileLine);
                calculateAverage(pointList);
                Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                        pointList.get(1), shellLayers, extruderWidth);
                previousPrintPoint = pointStart;
                gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                        + " Y" + formatNumber(pointStart.getY(), 3)
                        + " F" + formatNumber(travelSpeedValue, 3)
                        + "\n";


                for (int j = 0; j < shellLayers - 1; j++) {
                    generateEachShell(gcode, j, shellSpeed);
                }
                generateEachShell(gcode, shellLayers - 1, externalShellSpeed);


                gcode = generateSolidInfill(pointList, gcode, 0.0, infillSpeed,
                        lineGap * scaling, false);
                System.out.println("inner " + i + " finished.");
                fileLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateTop(String code) {
        String gcode = code;
        gcode = generateBottomTop(gcode, topLayers - 1);
        return gcode;
    }

    private static String generateLastTop(String code) {
        String gcode = code;
        Double height = zPrintList.get(fileLine);
        gcode += "G1 Z" + formatNumber(height, 3)
                + " F" + formatNumber(travelSpeedValue, 0)
                + "\n";
        lengthUsed -= retractLengthValue;
        gcode += "G1 E" + formatNumber(lengthUsed, 5)
                + " F" + formatNumber(retractSpeed, 5)
                + "\n";
        gcode += "G92 E0\n";
        ArrayList<Point> pointList = pointPrintList.get(fileLine);
        calculateAverage(pointList);
        Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                pointList.get(1), shellLayers, extruderWidth);
        previousPrintPoint = pointStart;
        gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                + " Y" + formatNumber(pointStart.getY(), 3)
                + " F" + formatNumber(travelSpeedValue, 3)
                + "\n";
        lengthUsed = retractLengthValue;
        gcode += "G1 E" + formatNumber(lengthUsed, 5)
                + " F" + formatNumber(retractSpeed, 5)
                + "\n";


        for (int j = 0; j < shellLayers - 1; j++) {
            generateEachShell(gcode, j, shellSpeed);
        }
        generateEachShell(gcode, shellLayers - 1, externalShellSpeed);


        lastTopSpeed = 40.0 * 60;
        lengthUsed -= retractLengthValue;
        gcode += "G1 E" + formatNumber(lengthUsed, 5)
                + " F" + formatNumber(retractSpeed, 5)
                + "\n";
        gcode += "G92 E0\n";
        lengthUsed = retractLengthValue;
        gcode = generateSolidInfill(pointList, gcode, Math.PI / 4, lastTopSpeed,
                extruderWidth * scaling, true);
        return gcode;
    }

    private static String generateEnd(String code) {
        String gcode = code;
        lengthUsed -= retractLengthValue;
        gcode += "G1 E" + formatNumber(lengthUsed, 5)
                + " F" + formatNumber(retractSpeed, 5)
                + "\n";
        gcode += "G92 E0\nM107\n";
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            FileInputStream fileEnd = new FileInputStream("File/endGcode.txt");
            BufferedReader readerEnd = new BufferedReader(new InputStreamReader(fileEnd));
            String endGcode = "";
            String line = readerEnd.readLine();
            while (line != null) {
                line = line.trim();
                endGcode += (line + "\n");
                line = readerEnd.readLine();
            }
            gcode += endGcode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        gcode += "M140 S0\n";
        return gcode;
    }
}