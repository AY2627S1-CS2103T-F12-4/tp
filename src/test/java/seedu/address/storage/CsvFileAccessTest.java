package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
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
}
