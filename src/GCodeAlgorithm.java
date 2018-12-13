import java.util.ArrayList;
import java.util.Comparator;

public class GCodeAlgorithm {

    static ArrayList<Point> removeDeletePoints(ArrayList<Point> pointDeleteList) {
        ArrayList<Point> pointResultList = new ArrayList<>();
        Boolean testDelete = false;
        for (int i = 0; i < pointDeleteList.size(); i++) {
            if (pointDeleteList.get(i).getY() > 63) {
                int temp = 0;
            }
            for (int j = 0; j < i; j++) {
                if (calculatePointDistance(pointDeleteList.get(i), pointDeleteList.get(j)) < GCodeParameters.extruderWidth / 3) {
                    testDelete = true;
                    break;
                }
            }
            if (!testDelete) {
                pointResultList.add(pointDeleteList.get(i));
            }
            testDelete = false;
        }
        return pointResultList;
    }

    static void generateEachShell() {
//        pointTempList.clear();
        calculateAverage(GCodeParameters.pointTempList);
        Boolean testDelete = false;
        ArrayList<Point> pointStoreList = new ArrayList<>();
        Point printPoint;
        for (int i = 1; i < GCodeParameters.pointTempList.size(); i++) {
            if (i == GCodeParameters.pointTempList.size() - 1) {
                printPoint = layerAlgorithm(GCodeParameters.pointTempList.get(i - 1), GCodeParameters.pointTempList.get(i),
                        GCodeParameters.pointTempList.get(0), 2, GCodeParameters.extruderWidth);
            } else {
                printPoint = layerAlgorithm(GCodeParameters.pointTempList.get(i - 1), GCodeParameters.pointTempList.get(i),
                        GCodeParameters.pointTempList.get(i + 1), 2, GCodeParameters.extruderWidth);

            }
//            if (printPoint.getY() < 0) {
//                int temp = 0;
//            }
            for (int j = 0; j < pointStoreList.size(); j++) {
                if (calculatePointDistance(printPoint, pointStoreList.get(j)) <= GCodeParameters.extruderWidth / 3) {
                    testDelete = true;
                    break;
                }
            }
            if (!testDelete) {
                pointStoreList.add(printPoint);
            }
            testDelete = false;
        }
        printPoint = layerAlgorithm(GCodeParameters.pointTempList.get(GCodeParameters.pointTempList.size() - 1), GCodeParameters.pointTempList.get(0),
                GCodeParameters.pointTempList.get(1), 2, GCodeParameters.extruderWidth);
        for (int j = 0; j < pointStoreList.size(); j++) {
            if (calculatePointDistance(printPoint, pointStoreList.get(j)) <= GCodeParameters.extruderWidth / 3) {
                testDelete = true;
                break;
            }
        }
        if (!testDelete) {
            pointStoreList.add(printPoint);
        }
//        testDelete = false;
        GCodeParameters.pointTempList = pointStoreList;
        GCodeParameters.shellList.add(GCodeParameters.pointTempList);
    }

    static String generateEachShellCode(String code, Integer index,
                                                Double shellSpeed, Double shellOutSpeed) {
        String gcode = code;
        Point pointStart;
        pointStart = GCodeParameters.shellList.get(GCodeParameters.shellList.size() - 1).get(0);
        GCodeParameters.previousPrintPoint = pointStart;
        gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                + " Y" + formatNumber(pointStart.getY(), 3)
                + " F" + formatNumber(GCodeParameters.travelSpeedValue, 3)
                + "\n";
        if (GCodeParameters.isRetract) {
            gcode += "G1 E" + formatNumber(GCodeParameters.retractLengthValue, 5)
                    + " F" + formatNumber(GCodeParameters.retractSpeed, 5)
                    + "\n";
            GCodeParameters.lengthUsed = GCodeParameters.retractLengthValue;
            GCodeParameters.isRetract = false;
        }

        Point printPoint;
        Double extrusion;
        for (int i = GCodeParameters.shellList.size() - 1; i >= 0; i--) {
            ArrayList<Point> pointStoreList = GCodeParameters.shellList.get(i);
            if (i != GCodeParameters.shellList.size() - 1) {
                pointStart = pointStoreList.get(0);
                gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                        + " Y" + formatNumber(pointStart.getY(), 3)
                        + " F" + formatNumber(GCodeParameters.travelSpeedValue, 3)
                        + "\n";
                GCodeParameters.previousPrintPoint = pointStart;
            }
            if (i > 0) gcode += "G1 F" + formatNumber(shellSpeed, 0) + "\n";
            else gcode += "G1 F" + formatNumber(shellOutSpeed, 0) + "\n";
            for (int j = 1; j < pointStoreList.size(); j++) {
                printPoint = pointStoreList.get(j);
                extrusion = extrusionData(GCodeParameters.previousPrintPoint, printPoint);
                GCodeParameters.lengthUsed += extrusion;
                gcode += "G1 X" + formatNumber(printPoint.getX(), 3)
                        + " Y" + formatNumber(printPoint.getY(), 3)
                        + " E" + formatNumber(GCodeParameters.lengthUsed, 5)
                        + "\n";
                GCodeParameters.previousPrintPoint = printPoint;
            }
            printPoint = pointStoreList.get(0);
            extrusion = extrusionData(GCodeParameters.previousPrintPoint, printPoint);
            GCodeParameters.lengthUsed += extrusion;
            gcode += "G1 X" + formatNumber(printPoint.getX(), 3)
                    + " Y" + formatNumber(printPoint.getY(), 3)
                    + " E" + formatNumber(GCodeParameters.lengthUsed, 5)
                    + "\n";
            GCodeParameters.previousPrintPoint = printPoint;
        }
        return gcode;
    }

    static String generateSolidInfill(ArrayList<Point> pointList, String code, Double lineAngle,
                                              Double solidInfillSpeed, Double gapOfInfillLine,
                                              Boolean ifRetraction) {
//        infillLineList.clear();
        GCodeParameters.infillLineNewList.clear();
        GCodeParameters.infillBoundPointList.clear();
        String gcode = code;
        Integer virtualLayer = GCodeParameters.shellLayers + 1;
//        gcode += "G1 F" + formatNumber(firstLayerSpeed, 0) + "\n";
        Point printPoint;

        generateEachShell();
//        printPoint = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                pointList.get(1), virtualLayer, extruderWidth);
        GCodeParameters.infillBoundPointList = GCodeParameters.pointTempList;
        GCodeParameters.infillLineAngle = lineAngle;
        constructInfillLines(gapOfInfillLine);
        Point pointStart = GCodeParameters.infillLineNewList.get(0).get(0);
        gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                + " Y" + formatNumber(pointStart.getY(), 3)
                + " F" + formatNumber(GCodeParameters.travelSpeedValue, 3)
                + "\n";
        GCodeParameters.previousPrintPoint = pointStart;
        if (ifRetraction) {
            gcode += "G1 E" + formatNumber(GCodeParameters.retractLengthValue, 5)
                    + " F" + formatNumber(GCodeParameters.retractSpeed, 5)
                    + "\n";
        }
        gcode += "G1 F" + formatNumber(solidInfillSpeed, 0) + "\n";
        Point pointEnd = GCodeParameters.infillLineNewList.get(0).get(GCodeParameters.infillLineNewList.get(0).size() - 1);
        Double extrusion = extrusionData(GCodeParameters.previousPrintPoint, pointEnd);
        GCodeParameters.lengthUsed += extrusion;
        gcode += "G1 X" + formatNumber(pointEnd.getX(), 3)
                + " Y" + formatNumber(pointEnd.getY(), 3)
                + " E" + formatNumber(GCodeParameters.lengthUsed, 5)
                + "\n";
        GCodeParameters.previousPrintPoint = pointEnd;
        for (int i = 1; i < GCodeParameters.infillLineNewList.size(); i++) {
            if (i % 2 == 1) {
                pointStart = GCodeParameters.infillLineNewList.get(i).get(GCodeParameters.infillLineNewList.get(0).size() - 1);
                pointEnd = GCodeParameters.infillLineNewList.get(i).get(0);
            } else {
                pointStart = GCodeParameters.infillLineNewList.get(i).get(0);
                pointEnd = GCodeParameters.infillLineNewList.get(i).get(GCodeParameters.infillLineNewList.get(0).size() - 1);
            }
            extrusion = extrusionData(GCodeParameters.previousPrintPoint, pointStart);
            GCodeParameters.lengthUsed += extrusion;
            gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
                    + " Y" + formatNumber(pointStart.getY(), 3)
                    + " E" + formatNumber(GCodeParameters.lengthUsed, 5)
                    + "\n";
            GCodeParameters.previousPrintPoint = pointStart;
            extrusion = extrusionData(GCodeParameters.previousPrintPoint, pointEnd);
            GCodeParameters.lengthUsed += extrusion;
            if (GCodeParameters.infillLineNewList.get(i).size() > 2) {
                Point pointNext = null;
                for (int j = 1; j < GCodeParameters.infillLineNewList.get(i).size() - 1; j++) {
                    if (j % 2 == 1) {
                        pointNext = GCodeParameters.infillLineNewList.get(i).get(j);
                        extrusion = extrusionData(GCodeParameters.previousPrintPoint, pointNext);
                        GCodeParameters.lengthUsed += extrusion;
                        gcode += "G1 X" + formatNumber(pointNext.getX(), 3)
                                + " Y" + formatNumber(pointNext.getY(), 3)
                                + " E" + formatNumber(GCodeParameters.lengthUsed, 5)
                                + "\n";
                        GCodeParameters.previousPrintPoint = pointNext;
                    } else {
                        GCodeParameters.lengthUsed -= GCodeParameters.retractLengthValue;
                        gcode += "G1 E" + formatNumber(GCodeParameters.lengthUsed, 5)
                                + " F" + formatNumber(GCodeParameters.retractSpeed, 5)
                                + "\n";
                        pointNext = GCodeParameters.infillLineNewList.get(i).get(j);
                        gcode += "G1 X" + formatNumber(pointNext.getX(), 3)
                                + " Y" + formatNumber(pointNext.getY(), 3)
                                + " F" + formatNumber(GCodeParameters.travelSpeedValue, 5)
                                + "\n";
                        GCodeParameters.lengthUsed += GCodeParameters.retractLengthValue;
                        gcode += "G1 E" + formatNumber(GCodeParameters.lengthUsed, 5)
                                + " F" + formatNumber(GCodeParameters.retractSpeed, 5)
                                + "\n";
                        GCodeParameters.previousPrintPoint = pointNext;
                    }
                }
            }
            gcode += "G1 X" + formatNumber(pointEnd.getX(), 3)
                    + " Y" + formatNumber(pointEnd.getY(), 3)
                    + " E" + formatNumber(GCodeParameters.lengthUsed, 5)
                    + "\n";
            GCodeParameters.previousPrintPoint = pointEnd;
        }
        return gcode;
    }

    private static void constructInfillLines(Double gapOfInfillLine) {
        Integer pointOrder = 0;
        Point pointStart = null, pointEnd = null, pointLineStore = null;
        Double startM, endM;
        String strResult = null;
        if (GCodeParameters.infillLineAngle < Math.PI / 2) {
            startM = -1 * Math.tan(GCodeParameters.infillLineAngle) * GCodeParameters.maxDim;
            endM = GCodeParameters.maxDim;
        } else {
            startM = 0.0;
            endM = GCodeParameters.maxDim - Math.tan(GCodeParameters.infillLineAngle) * GCodeParameters.maxDim;
        }

        for (Double m = startM; m <= endM; m += gapOfInfillLine) {
            ArrayList<Point> infillPointStoreList = new ArrayList<>();
            Double value1, value2;
            Double k = Math.tan(GCodeParameters.infillLineAngle);
            for (int i = 0; i < GCodeParameters.infillBoundPointList.size() - 1; i++) {
                value1 = k * GCodeParameters.infillBoundPointList.get(i).getX()
                        - GCodeParameters.infillBoundPointList.get(i).getY() + m;
                value2 = k * GCodeParameters.infillBoundPointList.get(i + 1).getX()
                        - GCodeParameters.infillBoundPointList.get(i + 1).getY() + m;
                strResult = String.valueOf(value1 * value2);
                if (strResult.charAt(0) == '-') {
                    pointLineStore = calculateCrossPoint(k, m, GCodeParameters.infillBoundPointList.get(i), GCodeParameters.infillBoundPointList.get(i + 1));
                    infillPointStoreList.add(pointLineStore);
                    pointOrder++;
                }
            }
            value1 = k * GCodeParameters.infillBoundPointList.get(GCodeParameters.infillBoundPointList.size() - 1).getX()
                    - GCodeParameters.infillBoundPointList.get(GCodeParameters.infillBoundPointList.size() - 1).getY() + m;
            value2 = k * GCodeParameters.infillBoundPointList.get(0).getX()
                    - GCodeParameters.infillBoundPointList.get(0).getY() + m;
            strResult = String.valueOf(value1 * value2);
            if (strResult.charAt(0) == '-') {
                pointLineStore = calculateCrossPoint(k, m,
                        GCodeParameters.infillBoundPointList.get(GCodeParameters.infillBoundPointList.size() - 1),
                        GCodeParameters.infillBoundPointList.get(0));
                infillPointStoreList.add(pointLineStore);
                pointOrder++;
            }
            if (pointOrder % 2 == 0 && pointOrder != 0) {
                infillPointStoreList = sortInfillLineByValue(infillPointStoreList);
                GCodeParameters.infillLineNewList.add(infillPointStoreList);
            }
            pointOrder = 0;
        }
    }

    private static ArrayList<Point> sortInfillLineByValue(ArrayList<Point> infillPointStoreList) {
        infillPointStoreList.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if ((o1.getY() - o2.getY()) < -1e-6) return 1;
                else if ((o1.getY() - o2.getY()) < 1e-6 && o1.getX() < o2.getX()) {
                    return 1;
                } else return -1;
            }
        });
        return infillPointStoreList;
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
        if (w1_1 * GCodeParameters.avgX + w1_2 * GCodeParameters.avgY + b_1 < 0) {
            C_1 = b_1 + (layer - 1) * d * Math.sqrt(Math.pow(w1_1, 2) + Math.pow(w1_2, 2));
        } else {
            C_1 = b_1 - (layer - 1) * d * Math.sqrt(Math.pow(w1_1, 2) + Math.pow(w1_2, 2));
        }
        if (w2_1 * GCodeParameters.avgX + w2_2 * GCodeParameters.avgY + b_2 < 0) {
            C_2 = b_2 + (layer - 1) * d * Math.sqrt(Math.pow(w2_1, 2) + Math.pow(w2_2, 2));
        } else {
            C_2 = b_2 - (layer - 1) * d * Math.sqrt(Math.pow(w2_1, 2) + Math.pow(w2_2, 2));
        }
        Double pointX, pointY;
        if (Math.abs(w2_2 * w1_1 - w2_1 * w1_2) > 1e-6) {
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

    static void calculateAverage(ArrayList<Point> pointList) {
        Double sumX = 0.0, sumY = 0.0;
        for (Point point : pointList) {
            sumX += point.getX();
            sumY += point.getY();
        }
        GCodeParameters.avgX = sumX / pointList.size();
        GCodeParameters.avgY = sumY / pointList.size();
    }

    static String formatNumber(Double number, Integer prefix) {
        String numberResult;
        numberResult = String.format("%." + String.valueOf(prefix) + "f", number);
        return numberResult;
    }

    private static Double extrusionData(Point previous, Point point) {
        Double length = Math.sqrt(Math.pow(point.getX() - previous.getX(), 2) +
                Math.pow(point.getY() - previous.getY(), 2));
        Double volume = length * GCodeParameters.extruderWidth * GCodeParameters.layerHeight;
        Double area = (Math.PI * Math.pow(GCodeParameters.filamentDiameter, 2)) / 4;
        return volume / area;
    }
}
