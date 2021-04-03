package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {

    private final static String serverPath = Path.of("").toAbsolutePath().toString();

    /**
     * Given the host and the url, get the CANONICAL path (unique, removed '../' ),
     * and ensure that it starts with the same sub-path Care must be given, if the
     * given parameters to not form a valid url, the method returns false, but not
     * because of a permission problem but because the object does not exist. Use
     * this method AFTER having checked the validity of the host + url
     * 
     * @param host
     * @param url
     * @return
     */
    public static boolean hasPermissions(String host, String url) {

        try {
            String canonicalObjectPath = new File(serverPath, "/" + host + "/" + url).getCanonicalPath();
            String canonicalHostPath = new File(serverPath, "/" + host).getCanonicalPath();

            if (canonicalObjectPath.startsWith(canonicalHostPath)) {
                return true;
            }
            return false;

        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }

    }

    public static String getFileType(String url) {
        String[] trimmedUrl = url.split("\\.");
        return url.equals("/") ? "text/html" : trimmedUrl[trimmedUrl.length - 1];
    }

    public static boolean isValidFile(String host, String url) {
        File f = new File(serverPath, host + url);
        return f.exists();
    }

    public static String getBody(String host, String url) throws IOException {
        String fileName = serverPath + "/" + host + url;
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

}
