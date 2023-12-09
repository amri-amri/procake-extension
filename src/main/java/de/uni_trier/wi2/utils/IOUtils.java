package de.uni_trier.wi2.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IOUtils {

    public static String getResourceAsString(String nameWithoutPackage) throws IOException, IOException {
        final String packageName = "/de/uni_trier/wi2";
        String nameWithPackage = nameWithoutPackage;
        if (nameWithPackage.charAt(0) != '/') nameWithPackage = "/" + nameWithPackage;
        nameWithPackage = packageName + nameWithPackage;
        InputStream is = IOUtils.class.getResourceAsStream(nameWithPackage);
        assert is != null;
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
}
