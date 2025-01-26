package com.mytool;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

@Command(
    name = "Mytool",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Command-line tool for generating and searching SBOMs."
)
public class MyTool implements Callable<Integer> {

    @Option(names = "--source", required = true, description = "The container image source (e.g., nginx).")
    private String source;

    @Option(names = "--search", description = "The package name regex to search in the SBOM (optional).")
    private String search;

    @Option(names = "--format", required = true, description = "The SBOM format (e.g., SPDX or Cyclonedx).")
    private String format;

    @Option(names = "-o", required = true, description = "The output file for the SBOM.")
    private String outputFile;

    @Override
    public Integer call() {
        // 1. Map user-friendly format to Syft-compatible format
        String syftFormat;
        switch (format.toLowerCase()) {
            case "spdx":
                syftFormat = "spdx-json";
                break;
            case "cyclonedx":
                syftFormat = "cyclonedx-json";
                break;
            default:
                System.err.println("Unsupported format: " + format 
                    + ". Try 'SPDX' or 'Cyclonedx'.");
                return 1;
        }

        // 2. Run Syft CLI to generate the SBOM
        // Command: syft <source> -o <syftFormat>
        ProcessBuilder processBuilder = new ProcessBuilder(
                "syft",
                source,
                "-o",
                syftFormat
        );

        // Redirect Syft’s standard output to the specified file
        processBuilder.redirectOutput(new File(outputFile));
        // Inherit Syft’s error output in the console, to see any error messages directly
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Syft exited with code " + exitCode);
                return exitCode;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }

        // 3. If --search was provided, parse the newly generated SBOM and find matches
        if (search != null && !search.isEmpty()) {
            searchPackagesInSbom(outputFile, search);
        } else {
            System.out.println("SBOM generated in file: " + outputFile);
            System.out.println("No search parameter provided, so no package search performed.");
        }

        return 0;
    }

    /**
     * Searches for packages/components matching the 'searchPattern' in the SBOM JSON file
     * and prints matching results.
     */
    private void searchPackagesInSbom(String sbomFilePath, String searchPattern) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(new File(sbomFilePath));

            // For SPDX JSON, packages are often found in `root["packages"]`
            // For CycloneDX JSON, components might be in `root["components"]`
            // Adjust as needed based on the actual Syft output.
            JsonNode packagesNode = root.get("packages");
            if (packagesNode == null) {
                // Attempt to get CycloneDX `components`
                packagesNode = root.get("components");
            }

            if (packagesNode == null || !packagesNode.isArray()) {
                System.out.println("No packages or components found in SBOM JSON.");
                return;
            }

            Pattern pattern = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE);
            boolean foundAny = false;

            for (JsonNode pkg : packagesNode) {
                // For SPDX, check `pkg["name"]`
                // For CycloneDX, `pkg["name"]` or `pkg["purl"]`
                JsonNode nameNode = pkg.get("name");
                if (nameNode != null) {
                    String pkgName = nameNode.asText();
                    if (pattern.matcher(pkgName).find()) {
                        foundAny = true;
                        System.out.println("Found package match: " + pkgName);
                    }
                }
            }

            if (!foundAny) {
                System.out.println("No packages matched search pattern: " + searchPattern);
            }

        } catch (IOException e) {
            System.err.println("Error reading SBOM file: " + e.getMessage());
        }
    }

    /**
     * Main entry point: creates and executes the Picocli command.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MyTool()).execute(args);
        System.exit(exitCode);
    }
}