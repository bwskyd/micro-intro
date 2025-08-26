package net.bwskyd.music.songservice.util;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.validation.constraints.NotNull;
import net.bwskyd.music.songservice.exception.CSVParseException;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public abstract class CSVUtil {
    public static List<Long> parseIDs(@NotNull String csv, @NotNull char delimiter) {
        if (csv.isEmpty()) {
            throw new CSVParseException("Invalid ID format: '%s'. Only positive integers are allowed".formatted(
                    csv.replaceAll("[0-9,]+", "")
            ));
        }
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(csv))
                .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build()).build()) {
            String[] values = reader.readNext();
            if (values == null) {
                throw new CSVParseException("Invalid ID format: '%s'. Only positive integers are allowed".formatted(
                        csv.replaceAll("[0-9,]+", "")
                ));
            }
            return Arrays.stream(values)
                    .filter(s -> !s.trim().isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (Exception e) {
            throw new CSVParseException("Invalid ID format: '%s'. Only positive integers are allowed".formatted(
                    csv.replaceAll("[0-9,]+", "")
            ));
        }
    }
}
