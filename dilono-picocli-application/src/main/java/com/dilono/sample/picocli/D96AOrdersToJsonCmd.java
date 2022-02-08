package com.dilono.sample.picocli;

import com.dilono.sample.basic.EdifactOrdersReader;
import com.dilono.sample.basic.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@CommandLine.Command(
    description = "Converter EDIFACT D96.A ORDERS to JSON"
)
@Component
public class D96AOrdersToJsonCmd implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(D96AOrdersToJsonCmd.class);
    private final ObjectMapper json;
    private final EdifactOrdersReader reader;
    @CommandLine.Option(names = {"-i", "--input-file"}, required = true, description = "Absolute path of a file to read from.")
    private String inputFile;
    @CommandLine.Option(names = {"-o", "--output-file"}, required = true, description = "Absolute path of a file to write to.")
    private String outputFile;

    public D96AOrdersToJsonCmd(final ObjectMapper json, final EdifactOrdersReader reader) {
        this.json = json;
        this.reader = reader;
    }

    @Override
    public void run() {
        if (!inputFile.startsWith(File.separator)) {
            throw new IllegalArgumentException(String.format("Absolute path for the input file '%s' must be provided.", inputFile));
        }
        if (!outputFile.startsWith(File.separator)) {
            throw new IllegalArgumentException(String.format("Absolute path for the out file '%s' must be provided.", outputFile));
        }
        final long startedAt = System.currentTimeMillis();
        LOG.info("Reading from {} ...", inputFile);
        try (final FileInputStream edifact = new FileInputStream(inputFile);) {
            final List<Order> orders = reader.fromEdifact(edifact);
            LOG.info("Read {} orders.", orders.size());

            final File destination = new File(outputFile);
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }

            LOG.info("Writing to '{}'.", outputFile);
            this.json.writerWithDefaultPrettyPrinter()
                .writeValue(destination, orders);
            LOG.info("File '{}' converted in {} ms.", outputFile, System.currentTimeMillis() - startedAt);
        } catch (Exception e) {
            LOG.error("{}", e.getMessage(), e);
        }
    }
}
