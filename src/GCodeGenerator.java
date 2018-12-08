import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;

public class GCodeGenerator {


    public static void generator() {
        GCodeParameters.maxDim = 100.0;
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
                GCodeParameters.zStrings.add(newLine);
                count++;
                line = readerZ.readLine();
            }
            readerZ.close();
            File fileZ = new File("File/zListFile.txt");
            FileWriter fileWriter = new FileWriter(fileZ);
            fileWriter.write("");
            for (String fileString : GCodeParameters.zStrings) {
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
            GCodeParameters.scaling = Integer.parseInt(properties.getProperty("scaling"));
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
                GCodeParameters.zPrintList.add(zData * GCodeParameters.scaling);
                String[] xPoints = lineX.split(" ");
                String[] yPoints = lineY.split(" ");
                ArrayList<Point> singleLayerPoints = new ArrayList<>();
                for (int i = 0; i < xPoints.length; i++) {
                    Point point = new Point(Double.parseDouble(xPoints[i]) * GCodeParameters.scaling,
                            Double.parseDouble(yPoints[i]) * GCodeParameters.scaling);
//                    point.setX(Double.parseDouble(xPoints[i]));
//                    point.setY(Double.parseDouble(yPoints[i]));
                    singleLayerPoints.add(point);
                }
                singleLayerPoints.remove(singleLayerPoints.size() - 1);
                GCodeParameters.pointPrintList.add(singleLayerPoints);
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
            GCodeParameters.firstBedTemperature = Double.parseDouble(firstBedTemp);
            gcode += ("M190 S" + GCodeAlgorithm.formatNumber(GCodeParameters.firstBedTemperature, 0) + "\n");
            String firstExtruderTemp = properties.getProperty("firstExtruderTemp");
            GCodeParameters.firstExtruderTemperature = Double.parseDouble(firstExtruderTemp);
            gcode += ("M104 S" + GCodeAlgorithm.formatNumber(GCodeParameters.firstExtruderTemperature, 0) + "\n");
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
            GCodeParameters.layerHeight = 0.0;
            GCodeParameters.fileLine = 0;
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            GCodeParameters.bedTemperature = Double.parseDouble(properties.getProperty("bedTemp"));
            GCodeParameters.extruderTemperature = Double.parseDouble(properties.getProperty("extruderTemp"));
            GCodeParameters.bottomLayers = Integer.parseInt(properties.getProperty("bottomLayers"));
            GCodeParameters.shellSpeed = Double.parseDouble(properties.getProperty("shellSpeed")) * 60;
            GCodeParameters.externalShellSpeed = Double.parseDouble(properties.getProperty("externalShellSpeed")) * 60;
            GCodeParameters.solidInfillSpeed = Double.parseDouble(properties.getProperty("solidInfillSpeed")) * 60;
            String travelSpeed = properties.getProperty("travelSpeed");
            String firstHeight = properties.getProperty("firstHeight");
            GCodeParameters.travelSpeedValue = Double.parseDouble(travelSpeed) * 60;
            double height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
            gcode += "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                    + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 3) + "\n";
            String retractLength = properties.getProperty("retractLength");
            GCodeParameters.retractLengthValue = Double.parseDouble(retractLength);
            GCodeParameters.retractSpeed = Double.parseDouble(properties.getProperty("retractSpeed")) * 60;
            gcode += "G1 E-" + GCodeAlgorithm.formatNumber(GCodeParameters.retractLengthValue, 5)
                    + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5) + "\n";
            gcode += "G92 E0\n";
            GCodeParameters.lengthUsed = -1 * GCodeParameters.retractLengthValue;
            gcode = generateFirstBottom(gcode, Math.PI / 4);
            gcode += "M104 S" + GCodeAlgorithm.formatNumber(GCodeParameters.extruderTemperature, 0) + "\n";
            gcode += "M140 S" + GCodeAlgorithm.formatNumber(GCodeParameters.bedTemperature, 0) + "\n";
            gcode = generateBottomTop(gcode, GCodeParameters.bottomLayers);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateFirstBottom(String code, Double lineAngle) {
        String gcode = code;
        GCodeParameters.layerHeight = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
        ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
        Properties properties = new Properties();
        try {
            GCodeParameters.shellList.clear();
            GCodeParameters.pointTempList.clear();
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            GCodeParameters.filamentDiameter = Double.parseDouble(properties.getProperty("filamentDiameter"));
//            calculateAverage(pointList);
            GCodeParameters.shellLayers = Integer.parseInt(properties.getProperty("shellLayers"));
            GCodeParameters.extruderWidth = Double.parseDouble(properties.getProperty("extruderWidth"));

            GCodeParameters.firstLayerSpeed = Double.parseDouble(properties.getProperty("firstLayerSpeed")) * 60;
//            lengthUsed = retractLengthValue;
            GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
            GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
            GCodeParameters.shellList.add(GCodeParameters.pointTempList);
            for (int i = 0; i < GCodeParameters.shellLayers - 1; i++) {
                GCodeAlgorithm.generateEachShell();
            }
            GCodeParameters.isRetract = true;
            gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.firstLayerSpeed, GCodeParameters.firstLayerSpeed);

            gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, lineAngle, GCodeParameters.firstLayerSpeed,
                    GCodeParameters.extruderWidth * GCodeParameters.scaling, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GCodeParameters.fileLine++;
        return gcode;
    }

    private static String generateBottomTop(String code, int layers) {
        String gcode = code;
        Double height;
        for (int i = 1; i <= layers; i++) {
            GCodeParameters.shellList.clear();
            GCodeParameters.pointTempList.clear();
            height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
            gcode += "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                    + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 0) + "\n";
            ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
//            Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                    pointList.get(1), shellLayers, extruderWidth);
//            previousPrintPoint = pointStart;
//            gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
//                    + " Y" + formatNumber(pointStart.getY(), 3)
//                    + " F" + formatNumber(travelSpeedValue, 3)
//                    + "\n";
            GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
            GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
            GCodeParameters.shellList.add(GCodeParameters.pointTempList);
            for (int j = 0; j < GCodeParameters.shellLayers - 1; j++) {
                GCodeAlgorithm.generateEachShell();
            }
//            generateEachShell();
            gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.shellSpeed, GCodeParameters.externalShellSpeed);

            if (i % 2 == 1)
                gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, 3 * Math.PI / 4, GCodeParameters.solidInfillSpeed,
                        GCodeParameters.extruderWidth * GCodeParameters.scaling, false);
            else gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, Math.PI / 4, GCodeParameters.solidInfillSpeed,
                    GCodeParameters.extruderWidth * GCodeParameters.scaling, false);
            GCodeParameters.fileLine++;
        }
        return gcode;
    }

    private static String generateInner(String code) {
        String gcode = code;
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            GCodeParameters.topLayers = Integer.parseInt(properties.getProperty("topLayers"));
            GCodeParameters.innerLayers = GCodeParameters.zPrintList.size() - GCodeParameters.topLayers - GCodeParameters.bottomLayers - 1;
            GCodeParameters.infillSpeed = Double.parseDouble(properties.getProperty("infillSpeed")) * 60;
            GCodeParameters.lineGap = Double.parseDouble(properties.getProperty("lineGap"));
//            gcode += "M106 S211.65\n";
            Double height;
            for (int i = 1; i <= GCodeParameters.innerLayers; i++) {
                GCodeParameters.shellList.clear();
                GCodeParameters.pointTempList.clear();
                height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
                gcode += "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                        + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 0) + "\n";
                ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
                GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
                GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
                GCodeParameters.shellList.add(GCodeParameters.pointTempList);
                for (int j = 0; j < GCodeParameters.shellLayers - 1; j++) {
                    GCodeAlgorithm.generateEachShell();
                }
                gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.shellSpeed, GCodeParameters.externalShellSpeed);

                gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, 0.0, GCodeParameters.infillSpeed,
                        GCodeParameters.lineGap * GCodeParameters.scaling, false);
                System.out.println("inner " + i + " finished.");
                GCodeParameters.fileLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateTop(String code) {
        String gcode = code;
        gcode = generateBottomTop(gcode, GCodeParameters.topLayers - 1);
        return gcode;
    }

    private static String generateLastTop(String code) {
        GCodeParameters.shellList.clear();
        GCodeParameters.pointTempList.clear();
        String gcode = code;
        Double height = GCodeParameters.zPrintList.get(GCodeParameters.fileLine);
        gcode += "G1 Z" + GCodeAlgorithm.formatNumber(height, 3)
                + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.travelSpeedValue, 0)
                + "\n";
        GCodeParameters.lengthUsed -= GCodeParameters.retractLengthValue;
        gcode += "G1 E" + GCodeAlgorithm.formatNumber(GCodeParameters.lengthUsed, 5)
                + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5)
                + "\n";
        gcode += "G92 E0\n";
        ArrayList<Point> pointList = GCodeParameters.pointPrintList.get(GCodeParameters.fileLine);
//        calculateAverage(pointList);
//        Point pointStart = layerAlgorithm(pointList.get(pointList.size() - 1), pointList.get(0),
//                pointList.get(1), shellLayers, extruderWidth);
//        previousPrintPoint = pointStart;
//        gcode += "G1 X" + formatNumber(pointStart.getX(), 3)
//                + " Y" + formatNumber(pointStart.getY(), 3)
//                + " F" + formatNumber(travelSpeedValue, 3)
//                + "\n";
//        lengthUsed = retractLengthValue;
//        gcode += "G1 E" + formatNumber(lengthUsed, 5)
//                + " F" + formatNumber(retractSpeed, 5)
//                + "\n";
        GCodeParameters.pointTempList = GCodeAlgorithm.removeDeletePoints(pointList);
        GCodeAlgorithm.calculateAverage(GCodeParameters.pointTempList);
        GCodeParameters.shellList.add(GCodeParameters.pointTempList);
        for (int j = 0; j < GCodeParameters.shellLayers - 1; j++) {
            GCodeAlgorithm.generateEachShell();
        }
        GCodeParameters.isRetract = true;
        gcode = GCodeAlgorithm.generateEachShellCode(gcode, 0, GCodeParameters.shellSpeed, GCodeParameters.externalShellSpeed);
        GCodeParameters.lastTopSpeed = 40.0 * 60;
        GCodeParameters.lengthUsed -= GCodeParameters.retractLengthValue;
        gcode += "G1 E" + GCodeAlgorithm.formatNumber(GCodeParameters.lengthUsed, 5)
                + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5)
                + "\n";
        gcode += "G92 E0\n";
        GCodeParameters.lengthUsed = GCodeParameters.retractLengthValue;
        gcode = GCodeAlgorithm.generateSolidInfill(pointList, gcode, Math.PI / 4, GCodeParameters.lastTopSpeed,
                GCodeParameters.extruderWidth * GCodeParameters.scaling, true);
        return gcode;
    }

    private static String generateEnd(String code) {
        String gcode = code;
        GCodeParameters.lengthUsed -= GCodeParameters.retractLengthValue;
        gcode += "G1 E" + GCodeAlgorithm.formatNumber(GCodeParameters.lengthUsed, 5)
                + " F" + GCodeAlgorithm.formatNumber(GCodeParameters.retractSpeed, 5)
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