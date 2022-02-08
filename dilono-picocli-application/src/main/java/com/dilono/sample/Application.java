package com.dilono.sample;

import com.dilono.sample.picocli.D96AOrdersToJsonCmd;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;


@CommandLine.Command(
    name = "",
    description = "Simple EDIFACT to JSON converter",
    mixinStandardHelpOptions = true
)
@SpringBootApplication
public class Application implements CommandLineRunner {

    private final D96AOrdersToJsonCmd ordersToJsonCmd;

    public Application(D96AOrdersToJsonCmd ordersToJsonCmd) {
        this.ordersToJsonCmd = ordersToJsonCmd;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        final CommandLine commandLine = new CommandLine(this);
        commandLine.addSubcommand("--d96a-orders-to-json", ordersToJsonCmd);
        System.exit(commandLine.execute(args));
    }
}
