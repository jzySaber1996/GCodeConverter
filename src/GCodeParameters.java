import java.util.ArrayList;

public class GCodeParameters {
    static Double lengthUsed;
    static Double layerHeight;
    static Integer scaling;
    static Double filamentDiameter;
    static Integer fileLine;
    static ArrayList<String> zStrings = new ArrayList<>();
    static Double maxDim;
    static Boolean isRetract = false;

    static ArrayList<Double> zPrintList = new ArrayList<>();
    static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();
    static ArrayList<Point> infillBoundPointList = new ArrayList<>();
    //    private static ArrayList<Line> infillLineList = new ArrayList<>();
    static ArrayList<Point> pointTempList = new ArrayList<>();
    static ArrayList<ArrayList<Point>> shellList = new ArrayList<>();
    static ArrayList<ArrayList<Point>> infillLineNewList = new ArrayList<>();

    static Double firstBedTemperature;
    static Double firstExtruderTemperature;
    static Double bedTemperature;
    static Double extruderTemperature;
    static Double travelSpeedValue;
    static Double retractSpeed;
    static Double retractLengthValue;
    static Double firstLayerSpeed;
    static Integer shellLayers;
    static Double avgX, avgY;
    static Double extruderWidth;
    static Point previousPrintPoint;
    static Double infillLineAngle;
    static Integer bottomLayers, innerLayers, topLayers;
    static Double lineGap;
    static Double shellSpeed, externalShellSpeed;
    static Double solidInfillSpeed, infillSpeed;
    static Double lastTopSpeed;
}
