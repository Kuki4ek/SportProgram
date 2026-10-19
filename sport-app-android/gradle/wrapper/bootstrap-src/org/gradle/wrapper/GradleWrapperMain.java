package org.gradle.wrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Minimal Gradle bootstrapper used in this generated sample project.
 * It downloads the distribution declared in gradle-wrapper.properties, caches
 * it in GRADLE_USER_HOME, and delegates all command-line arguments to Gradle.
 */
public final class GradleWrapperMain {
    private GradleWrapperMain() { }

    public static void main(String[] args) throws Exception {
        Path projectDir = findProjectDir(Paths.get(System.getProperty("user.dir")).toAbsolutePath());
        Path propertiesPath = projectDir.resolve("gradle/wrapper/gradle-wrapper.properties");
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(propertiesPath)) {
            properties.load(in);
        }

        String distributionUrl = properties.getProperty("distributionUrl");
        if (distributionUrl == null || distributionUrl.trim().isEmpty()) {
            throw new IOException("distributionUrl is missing in " + propertiesPath);
        }

        String fileName = distributionUrl.substring(distributionUrl.lastIndexOf('/') + 1);
        String folderName = fileName.replaceFirst("\\.zip$", "");
        Path gradleUserHome = Paths.get(System.getenv().getOrDefault(
            "GRADLE_USER_HOME", Paths.get(System.getProperty("user.home"), ".gradle").toString()));
        Path cacheDir = gradleUserHome.resolve("wrapper/dists/sportakus/" + folderName);
        Path gradleHome = cacheDir.resolve(folderName.replaceFirst("-(bin|all)$", ""));
        Path executable = gradleHome.resolve(isWindows() ? "bin/gradle.bat" : "bin/gradle");

        if (!Files.isRegularFile(executable)) {
            Files.createDirectories(cacheDir);
            Path zipPath = cacheDir.resolve(fileName);
            if (!Files.isRegularFile(zipPath)) {
                download(distributionUrl, zipPath);
            }
            unzipSafely(zipPath, cacheDir);
            Files.deleteIfExists(zipPath);
            if (!Files.isRegularFile(executable)) {
                // Some distribution archives use the folder name exactly as the zip name minus -bin/-all.
                try (var children = Files.list(cacheDir)) {
                    Path candidate = children
                        .filter(Files::isDirectory)
                        .filter(p -> Files.isRegularFile(p.resolve("bin/gradle")) || Files.isRegularFile(p.resolve("bin/gradle.bat")))
                        .findFirst().orElse(null);
                    if (candidate != null) {
                        gradleHome = candidate;
                        executable = gradleHome.resolve(isWindows() ? "bin/gradle.bat" : "bin/gradle");
                    }
                }
            }
            if (!Files.isRegularFile(executable)) {
                throw new IOException("Gradle was downloaded but its executable was not found in " + cacheDir);
            }
            if (!isWindows()) {
                makeExecutable(executable);
                makeExecutable(gradleHome.resolve("bin/gradle"));
            }
        }

        ProcessBuilder builder;
        if (isWindows()) {
            builder = new ProcessBuilder("cmd.exe", "/c", executable.toString());
            for (String arg : args) builder.command().add(arg);
        } else {
            builder = new ProcessBuilder(executable.toString());
            for (String arg : args) builder.command().add(arg);
        }
        builder.directory(projectDir.toFile()).inheritIO();
        int exitCode = builder.start().waitFor();
        System.exit(exitCode);
    }

    private static Path findProjectDir(Path start) throws IOException {
        Path current = start;
        while (current != null) {
            if (Files.isRegularFile(current.resolve("gradle/wrapper/gradle-wrapper.properties"))) return current;
            current = current.getParent();
        }
        throw new IOException("Could not find gradle/wrapper/gradle-wrapper.properties from " + start);
    }

    private static void download(String urlText, Path target) throws IOException {
        System.out.println("Downloading Gradle distribution: " + urlText);
        URL url = new URL(urlText);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(20_000);
        connection.setReadTimeout(60_000);
        connection.setRequestProperty("User-Agent", "SportakusProject-GradleBootstrap/1.0");
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("Gradle download failed with HTTP status " + status);
            }
            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 var output = new BufferedOutputStream(Files.newOutputStream(target))) {
                byte[] buffer = new byte[64 * 1024];
                int count;
                while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static void unzipSafely(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path output = targetDir.resolve(entry.getName()).normalize();
                if (!output.startsWith(targetDir.normalize())) {
                    throw new IOException("Unsafe path in Gradle distribution archive: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Path parent = output.getParent();
                    if (parent != null) Files.createDirectories(parent);
                    try (var out = new BufferedOutputStream(Files.newOutputStream(output))) {
                        byte[] buffer = new byte[64 * 1024];
                        int count;
                        while ((count = zip.read(buffer)) >= 0) out.write(buffer, 0, count);
                    }
                }
                zip.closeEntry();
            }
        }
    }

    private static void makeExecutable(Path path) {
        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(path, permissions);
        } catch (Exception ignored) {
            path.toFile().setExecutable(true, false);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
