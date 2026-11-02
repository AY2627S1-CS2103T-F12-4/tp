package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CsvFileAccessTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void resolve_relativeAndAbsolutePaths_areInsideHome() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home")).toRealPath();
        Files.createDirectory(home.resolve("reports"));
        CsvFileAccess files = new CsvFileAccess(home);
        Path output = home.resolve("reports/students.csv");
        assertEquals(output, files.forWriting(Path.of("reports/students.csv")));
        assertEquals(output, files.forWriting(output));
        assertEquals(output, files.forWriting(Path.of("reports/../reports/students.csv")));
    }

    @Test
    public void resolve_outsideTraversalAndPrefixSibling_areRejectedWithoutCreatingFiles() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home")).toRealPath();
        CsvFileAccess files = new CsvFileAccess(home);
        Path outside = temporaryFolder.resolve("outside.csv").toAbsolutePath();
        for (Path input : new Path[]{outside, Path.of("../outside.csv"), home.resolveSibling("home-copy/a.csv")}) {
            assertEquals(CsvFileAccess.MESSAGE_OUTSIDE_HOME,
                    assertThrows(IOException.class, () -> files.forWriting(input)).getMessage());
        }
        assertFalse(Files.exists(outside));
    }

    @Test
    public void resolve_missingDirectoriesAndHomeItself_areRejected() throws Exception {
        CsvFileAccess files = new CsvFileAccess(temporaryFolder);
        assertThrows(IOException.class, () -> files.forWriting(Path.of(".")));
        assertThrows(IOException.class, () -> files.forWriting(Path.of("missing/out.csv")));
        assertFalse(Files.exists(temporaryFolder.resolve("missing")));
    }

    @Test
    public void resolve_symbolicLinksToFilesOrDirectories_areRejected() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home")).toRealPath();
        Path outside = Files.createDirectory(temporaryFolder.resolve("outside")).toRealPath();
        Path target = Files.writeString(outside.resolve("input.csv"), "keep");
        try {
            Files.createSymbolicLink(home.resolve("linked-dir"), outside);
            Files.createSymbolicLink(home.resolve("linked.csv"), target);
            Files.createSymbolicLink(home.resolve("dangling.csv"), outside.resolve("absent.csv"));
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            assumeTrue(false, "Symbolic links are unavailable: " + e.getMessage());
        }
        CsvFileAccess files = new CsvFileAccess(home);
        for (String input : new String[]{"linked-dir/input.csv", "linked.csv", "dangling.csv"}) {
            assertEquals(CsvFileAccess.MESSAGE_SYMLINK,
                    assertThrows(IOException.class, () -> files.forWriting(Path.of(input))).getMessage());
        }
        assertEquals("keep", Files.readString(target));
        assertTrue(Files.isSymbolicLink(home.resolve("dangling.csv")));
    }

    @Test
    public void applicationHome_packagedJarWithSpaces_usesJarFolder() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("application folder"));
        Path jar = Files.createFile(home.resolve("roster.jar"));
        assertEquals(home, CsvFileAccess.applicationHome(jar.toUri().toURL()));
        assertEquals(home.toRealPath().resolve("report.csv"),
                new CsvFileAccess(CsvFileAccess.applicationHome(jar.toUri().toURL()))
                        .forWriting(Path.of("report.csv")));
    }

    @Test
    public void applicationHome_developmentClasses_usesWorkingFolder() throws Exception {
        assertEquals(Path.of("").toAbsolutePath(), CsvFileAccess.applicationHome(temporaryFolder.toUri().toURL()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void applicationHome_malformedClassLocation_explainsFailure() throws Exception {
        // A custom class loader can supply a URL containing an unescaped space.
        URL malformed = new URL("file:/invalid location/roster.jar");
        IllegalStateException failure = assertThrows(IllegalStateException.class, () ->
                CsvFileAccess.applicationHome(malformed));
        assertEquals("Could not determine Roster's home folder.", failure.getMessage());
        assertTrue(failure.getCause() instanceof URISyntaxException);
    }

    @Test
    public void resolve_homeDirectoryAlias_usesCanonicalHomeWithoutFollowingChildLinks() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("real-home")).toRealPath();
        Path alias = temporaryFolder.resolve("alias-home").toAbsolutePath();
        try {
            Files.createSymbolicLink(alias, home);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            assumeTrue(false, "Symbolic links are unavailable: " + e.getMessage());
        }
        CsvFileAccess files = new CsvFileAccess(alias);
        assertEquals(home.resolve("report.csv"), files.forWriting(alias.resolve("report.csv")));
        assertEquals(home.resolve("relative.csv"), files.forWriting(Path.of("relative.csv")));
    }
}
