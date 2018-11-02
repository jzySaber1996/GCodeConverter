import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class GCodeGenerator {
    private static Double lengthUsed;
    private static Double layerHeight;
    private static Integer fileLine;
    private static ArrayList<String> zStrings = new ArrayList<>();

    private static ArrayList<Double> zPrintList = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();

    private static Double travelSpeedValue;
    private static Double retractSpeed;
    private static Double retractLengthValue;
    private static Double firstLayerSpeed;
    private static Integer shellLayers;

    public static void generator() {
        reconstructFile();
        storeData();
        String code = "";
        code = generateStart(code);
        code = generateBasic(code);
        code = generateBottom(code);
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
            Double height = Double.valueOf(properties.getProperty("height"));
            Double firstHeight = Double.valueOf(properties.getProperty("firstHeight"));
            Integer count = 0;
            Double zHeight = 0.0;
            String line = readerZ.readLine();
            while (line != null){
                String[] data = line.split(" ");
                if (data[0].equals("")) break;
                zHeight = Double.parseDouble(data[0]);
                if (count == 0) {
                    zHeight += firstHeight;
                }
                else zHeight += height;
                String newLine = "";
                Integer length  = data.length;
                for (int i = 0; i < length; i++){
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
            for (String fileString : zStrings){
                fileWriter.write(fileString);
                fileWriter.write("\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void storeData(){
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
            while ((lineX != null) && (lineY != null) && (lineZ != null)){
                Double zData = Double.valueOf(lineZ.split(" ")[0]);
                zPrintList.add(zData);
                String[] xPoints = lineX.split(" ");
                String[] yPoints = lineY.split(" ");
                ArrayList<Point> singleLayerPoints = new ArrayList<>();
                for (int i=0; i < xPoints.length; i++){
                    Point point = new Point();
                    point.setX(Double.parseDouble(xPoints[i]));
                    point.setY(Double.parseDouble(yPoints[i]));
                    singleLayerPoints.add(point);
                }
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
            gcode += "G1 E-" + retractLength + " F" + retractLength + "\n";
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
            retractSpeed = Double.parseDouble(properties.getProperty("retractSpeed")) * 60;
            gcode += "G1 X" + String.valueOf(pointList.get(0).getX())
                    + " Y" + String.valueOf(pointList.get(0).getY())
                    + " F" + String.valueOf(travelSpeedValue)
                    + "\n";
            gcode += "G1 E" + String.valueOf(retractLengthValue)
                    + " F" + retractSpeed + "\n";
            shellLayers = Integer.parseInt(properties.getProperty("shellLayers"));
            firstLayerSpeed = Double.parseDouble(properties.getProperty("firstLayerSpeed")) * 60;
            for (int i = 0; i < shellLayers; i++){
                gcode = generateEachShell(gcode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileLine++;
        return gcode;
    }

    private static String generateEachShell(String code){
        String gcode = code;
        gcode += "G1 F" + String.valueOf(firstLayerSpeed) + "\n";
        
        return gcode;
    }
    private static String generateBottomSolidOther(String code) {
        String gcode = code;

        return gcode;
    }
}
