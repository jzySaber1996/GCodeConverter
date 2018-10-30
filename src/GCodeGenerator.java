import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GCodeGenerator {
    public static void generator(){
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
            properties.getProperty("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
