import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
//import java.io.InputStream;

/**
 * inform: the test matrix's dimension is 3
 * with the size of 176x265x201
 * the further more .mat file will change the size
 */
public class ReadFileFromMat {
    private static MLArray mlArray = null;//private static MLDouble mlDouble = null;
    public static double[][][] readFile() throws IOException {
        Properties properties = new Properties();
        properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
        //String filePath = properties.getProperty("filepath");
        MatFileReader read = new MatFileReader(properties.getProperty("filepath")); //seg1.mat
        String name = properties.getProperty("parameterName");
        MLArray mlArray = read.getMLArray(name); //I_Global
        if (properties.getProperty("fileType").equals("MLSingle")) {
            MLSingle d = (MLSingle) mlArray;//MLDouble d = (MLDouble) mlArray;
            properties.setProperty("xDim", String.valueOf(d.getDimensions()[0]));
            properties.setProperty("yDim", String.valueOf(d.getDimensions()[1]));
            properties.setProperty("zDim", String.valueOf(d.getDimensions()[2]));
            System.out.println(d.getDimensions()[0] + " " + d.getDimensions()[1] + " " + d.getDimensions()[2]);
            properties.storeToXML(new FileOutputStream("File/printParameters.xml"), "printInfo");
            double[][][] matrix = transfer_single(d);
            System.out.println("Read file finished!");
            return getLimitedSubVolume(matrix,100, 160, 50, 100, 0, 60);
        }
        else if (properties.getProperty("fileType").equals("MLDouble")){
            MLDouble d = (MLDouble) mlArray;//MLDouble d = (MLDouble) mlArray;
            properties.setProperty("xDim", String.valueOf(d.getDimensions()[0]));
            properties.setProperty("yDim", String.valueOf(d.getDimensions()[1]));
            properties.setProperty("zDim", String.valueOf(d.getDimensions()[2]));
            System.out.println(d.getDimensions()[0] + " " + d.getDimensions()[1] + " " + d.getDimensions()[2]);
            properties.storeToXML(new FileOutputStream("File/printParameters.xml"), "printInfo");
            double[][][] matrix = transfer_double(d);
            System.out.println("Read file finished!");
            return getLimitedSubVolume(matrix,100, 160, 50, 100, 0, 60);
        }
        return null;
    }

    private static double[][][] transfer_double(MLDouble d) {
        double[][][] matResult = new double[100][100][100];
        int count = 0;
        for (int i = 0; i < d.getDimensions()[0]; i++)
            for (int j = 0; j < d.getDimensions()[1]; j++)
                for (int k = 0; k < d.getDimensions()[2]; k++) {
                    matResult[i][j][k] = d.get(i, k * d.getDimensions()[1] + j);
                }
        return matResult;
    }

    private static double[][][] transfer_single(MLSingle d) {
        double[][][] matResult = new double[100][100][100];
        int count = 0;
        for (int i = 0; i < d.getDimensions()[0]; i++)
            for (int j = 0; j < d.getDimensions()[1]; j++)
                for (int k = 0; k < d.getDimensions()[2]; k++) {
                    matResult[i][j][k] = d.get(i, k * d.getDimensions()[1] + j);
                }
        return matResult;
    }

    private static double[][][] getLimitedSubVolume(double[][][] matInit,
                                                    int x_start, int x_end,
                                                    int y_start, int y_end,
                                                    int z_start, int z_end) {
        double[][][] matResult = new double[100][100][100];
        for (int i = 0; i <= x_end - x_start; i++)
            for (int j = 0; j <= y_end - y_start; j++)
                for (int k = 0; k <= z_end - z_start; k++) {
                    matResult[i][j][k] = matInit[i + x_start][j + y_start][k + z_start];
                }
        return matResult;
    }
}
