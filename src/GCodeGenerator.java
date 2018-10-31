import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GCodeGenerator {
    public static void generator() {
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
            double travelSpeedValue = Double.parseDouble(travelSpeed) * 60;
            double height = Double.parseDouble(firstHeight);
            gcode += "G1 Z" + firstHeight + " F" + String.valueOf(travelSpeedValue) + "\n";
            String retractLength = properties.getProperty("retractLength");
            Double retractLengthValue = Double.parseDouble(retractLength);
            gcode += "G1 E-" + retractLength + " F" + retractLength + "\n";
            gcode += "G92 E0\n";
            Double lengthUsed = -1 * retractLengthValue;
            gcode = generateBottomSolid(gcode, lengthUsed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcode;
    }

    private static String generateBottomSolid(String code, Double lengthUsed) {
        String gcode = code;

        return gcode;
    }
}
