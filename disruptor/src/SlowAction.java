import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author pblinov
 * @since 12/02/2015
 */
public class SlowAction {
    public static void doSomething(Event event) {
        try {
            File tempFile = File.createTempFile("AAA", "tmp");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(tempFile));
            outputStream.writeObject(event);
            outputStream.close();
            tempFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
