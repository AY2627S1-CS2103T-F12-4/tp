package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Restricts CSV operations to ordinary files inside the application's home folder.
 */
public class CsvFileAccess {
    public static final String MESSAGE_OUTSIDE_HOME = "CSV files must be inside the folder containing Roster's JAR.";
    public static final String MESSAGE_SYMLINK = "CSV paths must not contain symbolic links.";
    public static final String MESSAGE_INVALID_PATH = "Provide a valid CSV file path ending in .csv.";

    private final Path home;

    public CsvFileAccess() {
        this(applicationHome());
    }

    /**
     * Creates an access policy rooted at {@code home}; also used by isolated tests.
     */
    public CsvFileAccess(Path home) {
        this.home = requireNonNull(home).toAbsolutePath().normalize();
    }

    /** Returns whether a path has a CSV filename, ignoring the extension's letter case. */
    public static boolean isCsvPath(Path path) {
        requireNonNull(path);
        return path.getFileName() != null
                && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    /**
     * Resolves a CSV output whose parent already exists inside the home folder.
     */
    public Path forWriting(Path output) throws IOException {
        Path resolved = resolve(output);
        if (!Files.isDirectory(resolved.getParent(), LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The CSV destination folder does not exist. Create it first.");
        }
        return resolved;
    }

    private Path resolve(Path supplied) throws IOException {
        requireNonNull(supplied);
        Path root = home.toRealPath();
        Path target = supplied.isAbsolute() ? supplied.normalize() : root.resolve(supplied).normalize();
        if (!target.startsWith(root) && target.startsWith(home)) {
            target = root.resolve(home.relativize(target));
        }
        if (!target.startsWith(root) || target.equals(root)) {
            throw new IOException(MESSAGE_OUTSIDE_HOME);
        }
        Path current = root;
        for (Path part : root.relativize(target)) {
            current = current.resolve(part);
            if (Files.isSymbolicLink(current)) {
                throw new IOException(MESSAGE_SYMLINK);
            }
        }
        return target;
    }

    private static Path applicationHome() {
        try {
            Path location = Path.of(CsvFileAccess.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // Development runs load classes from a directory; packaged runs load them from the JAR.
            return Files.isDirectory(location) ? Path.of("").toAbsolutePath() : location.getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not determine Roster's home folder.", e);
        }
    }
}
