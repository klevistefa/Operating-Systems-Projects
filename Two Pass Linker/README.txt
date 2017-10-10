Klevis Tefa

CSCI-UA 202-002: Operating Systems

Lab Assignment #1: Dual Pass Linker

September 19th 2017

Given an input file as the first command line argument the program will print the output on the console.

Instructions on how to run:

1. Compile:
While in terminal navigate to the directory that contains the source code (Linker.java and MetaData.java)
and run the following command:
  javac *.java

2. Run:
Use the following command line: java Linker input/file.
input/file is the path to the input file.
Make sure to put the input file in the input folder.
Web input files(input-1 to input-8) have been included as well.

3. Files:
Linker.java - Contains the two pass linker and produces the output.
MetaData.java - Contains the data that the Linker.java file needs to produce its output.

4.Example
Provided that you are in the same directory as the source code and input folder:
javac *.java
java Linker ./input/input-1

The command above runs the linker with input file input-1 and displays the output on the console.
