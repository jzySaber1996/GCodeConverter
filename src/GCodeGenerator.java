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

    private static ArrayList<Double> zPrintList = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();
    private static ArrayList<Point> infillBoundPointList = new ArrayList<>();
    private static ArrayList<Line> infillLineList = new ArrayList<>();

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
    private static Integer bottomLayers;
    private static Double lineGap;
    private static Double shellSpeed, externalShellSpeed;
    private static Double solidInfillSpeed;

    public static void generator() {
        maxDim = 100.0;
        reconstructFile();
        storeData();
        String code = "";
        code = generateStart(code);
        code = generateBasic(code);
        code = generateBottom(code);
        try {
            FileWriter fileWriter = new FileWriter(new File("File/Output.txt"));
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
            gcode += properties.getProperty("startGcode");
            gcode += "\n";
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
            gcode = generateFirstBottom(gcode, Math.PI/4);
            gcode = generateBottomOther(gcode);
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
            Point pointStart;
            pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                    pointList.get(1), shellLayers, extruderWidth);
            previousPrintPoint = pointStart;
            gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                    + " Y" + formatNumber(pointStart.getY(), 3)
                    + " F" + formatNumber(travelSpeedValue, 3)
                    + "\n";
            gcode += "G1 E" + formatNumber(retractLengthValue, 5)
                    + " F" + formatNumber(retractSpeed, 5)
                    + "\n";
            lengthUsed = retractLengthValue;
            firstLayerSpeed = Double.parseDouble(properties.getProperty("firstLayerSpeed")) * 60;
            for (int i = 0; i < shellLayers; i++) {
                gcode = generateEachShell(pointList, gcode, i, firstLayerSpeed);
            }
            gcode = generateSolidInfill(pointList, gcode, lineAngle, firstLayerSpeed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileLine++;
        return gcode;
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

    private static String generateEachShell(ArrayList<Point> pointList, String code, Integer index,
                                            Double shellSpeed) {
        String gcode = code;
        gcode += "G1 F" + formatNumber(shellSpeed, 0) + "\n";
        Integer layer = shellLayers - index;
        Point printPoint;
        Double extrusion;
        for (int i = 1; i < pointList.size(); i++) {
            if (i == pointList.size() - 1) {
                printPoint = layerAlgorithm(pointList.get(i - 1), pointList.get(i),
                        pointList.get(0), layer, extruderWidth);
            } else {
                printPoint = layerAlgorithm(pointList.get(i - 1), pointList.get(i),
                        pointList.get(i + 1), layer, extruderWidth);
            }
            extrusion = extrusionData(previousPrintPoint, printPoint);
            lengthUsed += extrusion;
            gcode += "G1 X" + formatNumber(printPoint.getX(), 3)
                    + " Y" + formatNumber(printPoint.getY(), 3)
                    + " E" + formatNumber(lengthUsed, 5)
                    + "\n";
            previousPrintPoint = printPoint;
        }
        printPoint = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                pointList.get(1), layer, extruderWidth);
        extrusion = extrusionData(previousPrintPoint, printPoint);
        lengthUsed += extrusion;
        gcode += "G1 X" + formatNumber(printPoint.getX(), 3)
                + " Y" + formatNumber(printPoint.getY(), 3)
                + " E" + formatNumber(lengthUsed, 5)
                + "\n";
        Point pointNewStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                pointList.get(1), layer - 1, extruderWidth);
        previousPrintPoint = pointNewStart;
        gcode += "G1 X" + formatNumber(pointNewStart.getX(), 3)
                + " Y" + formatNumber(pointNewStart.getY(), 3)
                + " F" + formatNumber(travelSpeedValue, 3)
                + "\n";
        return gcode;
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

    private static String generateSolidInfill(ArrayList<Point> pointList, String code, Double lineAngle,
                                              Double solidInfillSpeed) {
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
        constructInfillLines();
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

    private static void constructInfillLines() {
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
        for (Double m = startM; m <= endM; m += extruderWidth * scaling) {
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

    private static String generateBottomOther(String code) {
        String gcode = code;
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            bedTemperature = Double.parseDouble(properties.getProperty("bedTemp"));
            extruderTemperature = Double.parseDouble(properties.getProperty("extruderTemp"));
            bottomLayers = Integer.parseInt(properties.getProperty("bottomLayers"));
            shellSpeed = Double.parseDouble(properties.getProperty("shellSpeed")) * 60;
            externalShellSpeed = Double.parseDouble(properties.getProperty("externalShellSpeed")) * 60;
            solidInfillSpeed = Double.parseDouble(properties.getProperty("solidInfillSpeed")) * 60;

            gcode += "M104 S" + formatNumber(extruderTemperature, 0) + "\n";
            gcode += "M140 S" + formatNumber(bedTemperature, 0) + "\n";
            Double height;
            for (int i = 1; i <= bottomLayers; i++) {
                height = zPrintList.get(fileLine);
                gcode += "G1 Z" + formatNumber(height, 3)
                        + " F" + formatNumber(travelSpeedValue, 0) + "\n";
                ArrayList<Point> pointList = pointPrintList.get(fileLine);
                Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                        pointList.get(1), shellLayers, extruderWidth);
                previousPrintPoint = pointStart;
                gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                        + " Y" + formatNumber(pointStart.getY(), 3)
                        + " F" + formatNumber(travelSpeedValue, 3)
                        + "\n";
                for (int j = 0; j < shellLayers - 1; j++) {
                    gcode = generateEachShell(pointList, gcode, j, shellSpeed);
                }
                gcode = generateEachShell(pointList, gcode, shellLayers - 1, externalShellSpeed);
                if (i % 2 == 1)
                    gcode = generateSolidInfill(pointList, gcode, 3 * Math.PI/4, solidInfillSpeed);
                else gcode = generateSolidInfill(pointList, gcode, Math.PI/4, solidInfillSpeed);
                fileLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }
}