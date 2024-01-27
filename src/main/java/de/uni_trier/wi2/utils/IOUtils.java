package de.uni_trier.wi2.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.METHOD_CALL;
import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.maxSubstring;

public class IOUtils {

    public static String getResourceAsString(String nameWithoutPackage) throws IOException, IOException {
        METHOD_CALL.trace(
                "public static String procake-extension.utils.IOUtils.getResourceAsString(String nameWithoutPackage={})...",
                nameWithoutPackage);

        final String packageName = "/de/uni_trier/wi2";
        String nameWithPackage = nameWithoutPackage;
        if (nameWithPackage.charAt(0) != '/') nameWithPackage = "/" + nameWithPackage;
        nameWithPackage = packageName + nameWithPackage;
        InputStream is = IOUtils.class.getResourceAsStream(nameWithPackage);
        assert is != null;

        String out = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        METHOD_CALL.trace("procake-extension.utils.IOUtils.getResourceAsString(String): return {}", maxSubstring(out));
        return out;
    }
}
