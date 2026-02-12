package dev.ayam.folderinspector.writer.console;

import dev.ayam.folderinspector.core.plugin.Writer;
import dev.ayam.folderinspector.writer.console.utility.StringResource;

/**
 * Implementation of {@link Writer} that writes to Console.
 */
/**
 * Implementation of {@link Writer} that outputs data to the system console.
 */
public class ConsoleWriter implements Writer {

    /**
     * Writes the provided content stream to standard output.
     *
     * @param content The stream of formatted strings to write.
     * @return "Console" indicating the output destination.
     */
    @Override
    public String write(java.util.stream.Stream<String> content) {
        if (content != null) {
            content.forEach(System.out::println);
        }
        return "Console";
    }

    /**
     * Returns the name of this writer.
     *
     * @return "console"
     */
    @Override
    public String getName() {
        return StringResource.NAME;
    }
}
