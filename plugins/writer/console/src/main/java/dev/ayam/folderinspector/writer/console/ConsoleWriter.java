package dev.ayam.folderinspector.writer.console;

import dev.ayam.folderinspector.core.plugin.Writer;
import dev.ayam.folderinspector.writer.console.utility.StringResource;

/**
 * Implementation of {@link Writer} that writes to Console.
 */
public class ConsoleWriter implements Writer {

    @Override
    public String write(java.util.stream.Stream<String> content) {
        if (content != null) {
            content.forEach(System.out::println);
        }
        return "Console";
    }

    @Override
    public String getName() {
        return StringResource.NAME;
    }
}
