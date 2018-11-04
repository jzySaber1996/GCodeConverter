import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class GCodeGenerator {
    private static Double lengthUsed;
    private static Double layerHeight;
    private static Double filamentDiameter;
    private static Integer fileLine;
    private static ArrayList<String> zStrings = new ArrayList<>();

    private static ArrayList<Double> zPrintList = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();
    private static ArrayList<Point> layerPointList = new ArrayList<>();

    private static Double travelSpeedValue;
    private static Double retractSpeed;
    private static Double retractLengthValue;
    private static Double firstLayerSpeed;
    private static Integer shellLayers;
    private static Double avgX, avgY;
    private static Double extruderWidth;
    private static Point previousPrintPoint;

    public static void generator() {
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
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            properties.getProperty("");
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
                zPrintList.add(zData);
                String[] xPoints = lineX.split(" ");
                String[] yPoints = lineY.split(" ");
                ArrayList<Point> singleLayerPoints = new ArrayList<>();
                for (int i = 0; i < xPoints.length; i++) {
                    Point point = new Point(Double.parseDouble(xPoints[i]),
                            Double.parseDouble(yPoints[i]));
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
            gcode += ("M190 S" + firstBedTemp + "\n");
            String firstExtruderTemp = properties.getProperty("firstExtruderTemp");
            gcode += ("M104 S" + firstExtruderTemp + "\n");
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
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            String travelSpeed = properties.getProperty("travelSpeed");
            String firstHeight = properties.getProperty("firstHeight");
            travelSpeedValue = Double.parseDouble(travelSpeed) * 60;
            double height = Double.parseDouble(firstHeight);
            gcode += "G1 Z" + firstHeight + " F" + String.valueOf(travelSpeedValue) + "\n";
            String retractLength = properties.getProperty("retractLength");
            retractLengthValue = Double.parseDouble(retractLength);
            retractSpeed = Double.parseDouble(properties.getProperty("retractSpeed")) * 60;
            gcode += "G1 E-" + String.valueOf(retractLength)
                    + " F" + String.valueOf(retractSpeed) + "\n";
            gcode += "G92 E0\n";
            lengthUsed = -1 * retractLengthValue;
            layerHeight = 0.0;
            fileLine = 0;
            gcode = generateFirstSolid(gcode);
            gcode = generateBottomSolidOther(gcode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateFirstSolid(String code) {
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
            gcode += "G1 X" + String.valueOf(pointStart.getX())
                    + " Y" + String.valueOf(pointStart.getY())
                    + " F" + String.valueOf(travelSpeedValue)
                    + "\n";
            gcode += "G1 E" + String.valueOf(retractLengthValue)
                    + " F" + retractSpeed + "\n";
            lengthUsed = retractLengthValue;
            firstLayerSpeed = Double.parseDouble(properties.getProperty("firstLayerSpeed")) * 60;
            for (int i = 0; i < shellLayers; i++) {
                gcode = generateEachShell(pointList, gcode, i);
            }
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

    private static String generateEachShell(ArrayList<Point> pointList, String code, Integer index) {
        String gcode = code;
        gcode += "G1 F" + String.valueOf(firstLayerSpeed) + "\n";
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
            gcode += "G1 X" + String.valueOf(printPoint.getX())
                    + " Y" + String.valueOf(printPoint.getY())
                    + " E" + String.valueOf(lengthUsed)
                    + "\n";
            previousPrintPoint = printPoint;
        }
        printPoint = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                pointList.get(1), layer, extruderWidth);
        extrusion = extrusionData(previousPrintPoint, printPoint);
        lengthUsed += extrusion;
        gcode += "G1 X" + String.valueOf(printPoint.getX())
                + " Y" + String.valueOf(printPoint.getY())
                + " E" + String.valueOf(lengthUsed)
                + "\n";
        Point pointNewStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
                pointList.get(1), layer - 1, extruderWidth);
        previousPrintPoint = pointNewStart;
        gcode += "G1 X" + String.valueOf(pointNewStart.getX())
                + " Y" + String.valueOf(pointNewStart.getY())
                + " F" + String.valueOf(travelSpeedValue)
                + "\n";
        return gcode;
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
        Double pointX = 0.0, pointY = 0.0;
        if (w2_2 * w1_1 - w2_1 * w1_2 != 0) {
            pointX = (w1_2 * C_2 - w2_2 * C_1) / (w2_2 * w1_1 - w2_1 * w1_2);
            pointY = (w1_1 * C_2 - w2_1 * C_1) / (w1_2 * w2_1 - w2_2 * w1_1);
        }
        else {
            Double w3_1 = w1_2;
            Double w3_2 = -1 * w1_1;
            Double C_3 = w1_1 * point.getY() - w1_2 * point.getX();
            pointX = (w1_2 * C_3 - w3_2 * C_1) / (w3_2 * w1_1 - w3_1 * w1_2);
            pointY = (w1_1 * C_3 - w3_1 * C_1) / (w1_2 * w3_1 - w3_2 * w1_1);
        }
        return new Point(pointX, pointY);
    }

    private static String generateBottomSolidOther(String code) {
        String gcode = code;

        return gcode;
    }
}
