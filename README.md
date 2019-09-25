# CLI tool to generate BP Passports for the Simple app

## Building the project
1. Install JDK 8
2. Import the project into IntelliJ IDEA. This is optional if you don't want to modify the project. If you only need to build the project, skip to step 3.
3. To build the project, run `./gradlew shadowJar` from the project directory.
    * The JAR should be generated at `build/libs/bppassportgen-{version}-all.jar`.
    * The project version will be defined in `build.gradle`.
    
## Using the JAR to generate BP Passports
1. Install [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).
2. After installing Java 8, ensure it is available in the command line. Open the terminal and run `java -version`. It should print something like 

        java version "1.8.0_172"
        Java(TM) SE Runtime Environment (build 1.8.0_172-b11)
        Java HotSpot(TM) 64-Bit Server VM (build 25.172-b11, mixed mode)
       
3. Navigate in the terminal to the location to where the JAR is present.
4. To run the tool, run the following command

    `java -jar bppassportgen-{version}-all.jar -c 1 -t <path to passport template file> -o ~/Desktop/Passports`

5. The following options are available to configure the generated PDFs
  * `-c,--count <arg>`           Number of BP Passports to generate
  * `-h,--help`                  Print this message
  * `-o,--output <arg>`          Directory to save the generated BP passports
  * `-p,--pages <arg>`           Number of pages in the PDF
  * `-rc,--row-count <arg>`      Number of rows in a page
  * `-cc,--column-count <arg>`   Number of columns in a page
  * `-t,--template <arg>`        Path to the template file
  * `-sticker`                   Generate stickers instead of BP Passports

For example, if you want to generate a total of 10000 passports, with each page 2 X 2 and a total of 40 passports in a single PDF, run the following command:

`java -jar bppassportgen-{version}-all.jar -c 10000 -t bppassport-template.pdf -p 10 -rc 2 -cc 2 -o ~/Desktop/Passports`

For generating stickers, just add the `-sticker` flag in the command line.

`java -jar bppassportgen-{version}-all.jar -c 10000 -t bppassport-template.pdf -p 10 -rc 2 -cc 2 -sticker -o ~/Desktop/Passports`
