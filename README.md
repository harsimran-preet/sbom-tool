# **SBOM-Tool**

**SBOM-Tool** is a Java-based command-line application that:

* Uses [Syft](https://github.com/anchore/syft) to generate Software Bill of Materials (SBOM) for container images.  
* Supports multiple SBOM formats (`SPDX` or `Cyclonedx`).  
* Allows optional **regex-based** search to find packages within the SBOM.

## **Table of Contents**

1. Features  
2. Requirements  
3. Quick Start (Local)  
4. Usage  
5. Docker Instructions  
6. Release Binaries / Cross-Platform  
7. Project Structure  
8. Future Enhancements  
9. License

---

## **Features**

* **Generate SBOMs** using Syft for any container image.  
* **Choose an SBOM format** (`SPDX` or `Cyclonedx`).  
* **Regex-based search** of package names in the generated SBOM (optional).  
* **Extensible & maintainable** CLI design using [Picocli](https://picocli.info/) and [Jackson](https://github.com/FasterXML/jackson).

---

## **Requirements**

* **Java 17+** installed (if running locally without Docker).  
* **Maven 3.9+** (to build the project).  
* **Docker 20+** (if running in a Docker container).

---

## **Quick Start (Local)**

### **1\. Clone the Repository**

git clone https://github.com/harsimran-preet/sbom-tool  
cd sbom-tool

### **2\. Build the Fat JAR**

mvn clean package

* This creates a “fat” (uber) JAR in the `target/` folder (e.g., `sbom-tool-1.0-SNAPSHOT.jar`) that includes all dependencies.

### **3\. Run the Tool**

java \-jar target/sbom-tool-1.0-SNAPSHOT.jar \\  
    \--source nginx \\  
    \--format SPDX \\  
    \-o sbom.json \\  
    \--search "lib.\*"

* **`--source nginx`**: Which container image to analyze.  
* **`--format SPDX`**: SBOM format (`SPDX` or `Cyclonedx`).  
* **`-o sbom.json`**: Output SBOM file.  
* **`--search "lib.*"`**: (Optional) regex for package names.

**Result**: A file `sbom.json` is created, and any matching packages are listed in the console.

---

## **Usage**

You can customize your command line parameters:

* **Required**:

  * `--source {imageName}` (e.g., `nginx`, `ubuntu:latest`, etc.)  
  * `--format {SPDX|Cyclonedx}`  
  * `-o {outputFile}`  
* **Optional**:

  * `--search {regex}`

**Example**:

java \-jar target/sbom-tool-1.0-SNAPSHOT.jar \\  
    \--source ubuntu:latest \\  
    \--format Cyclonedx \\  
    \-o ubuntu-sbom.json

*(No `--search` provided, so it simply generates the SBOM without searching.)*

---

## **Docker Instructions**

### **1\. Build the Docker Image Locally**

From the project root (where the `Dockerfile` is located):

docker build \-t sbom-tool:latest .

This uses the **`openjdk:17-jdk-slim`** (or similar) base image and installs **Syft** plus your fat JAR.

### **2\. Run the Docker Container**

docker run \--rm sbom-tool:latest \\  
    \--source nginx \\  
    \--format SPDX \\  
    \-o sbom.json \\  
    \--search "lib.\*"

* The container will generate the SBOM inside the Docker environment and print regex matches to the console.

### **3\. Run the published Docker Image**

You now have a **public Docker image** on Docker Hub. Anyone with Docker installed can run:

bash

`docker run --rm trippygander/sbom-tool:latest \`

  `--source <image> \`

  `--format <SPDX|Cyclonedx> \`

  `-o output.json \`

  `[--search <regex>]`

`Example run:`

`docker pull trippygander/sbom-tool:latest`

`docker run --rm trippygander/sbom-tool:latest \`

  `--source nginx \`

  `--format SPDX \`

  `-o sbom.json \`

  `--search "lib.*"`

---

## **Release Binaries / Cross-Platform**

The assignment requests a tool release for **amd64/arm/windows**. In Java, **fat JAR** is already cross-platform as long as the user has Java 17 installed.

### **Provided Fat JAR \+ Scripts**

1. `target/sbom-tool-1.0-SNAPSHOT.jar` file has been released on the GitHub repo  
2. (Optional) Here are provided small scripts for convenience:

**`run.sh`** (Linux/macOS):  
 \#\!/usr/bin/env bash  
java \-jar sbom-tool-1.0-SNAPSHOT.jar "$@"

* 

**`run.bat`** (Windows):  
 @echo off  
java \-jar sbom-tool-1.0-SNAPSHOT.jar %\*

*   
3. This way, users can just do `./run.sh --source nginx --format SPDX -o sbom.json`.

---

## **Project Structure**

sbom-tool/  
├── Dockerfile  
├── pom.xml  
├── README.md  
├── src/  
│   ├── main/java/com/mytool/MyTool.java  
│   └── test/java/... (optional tests)  
└── target/  
    └── sbom-tool-1.0-SNAPSHOT.jar (generated fat JAR)

---

## **Future Enhancements**

* **Add more SBOM formats**: e.g., `spdx-tag-value`, `table` (for debugging), etc.  
* **Detailed searching**: match on version, license, or other SBOM fields.  
* **Integration tests**: ensure Docker-based runs are tested in CI pipelines.

---

## **Final Notes**

* **Thank you** for reviewing this submission\!  
* Please see the above instructions to build/run the Docker image, or simply grab the prebuilt Docker image if published.  
* The JAR is cross-platform and only requires Java 17+.

Enjoy exploring the **SBOM-Tool**\!

