# Boolean Retrieval using Lucene Index 
#### Built By Aneesh Bhatnagar

This project was built as a part of the Information Retrieval Course at University at Buffalo. 
The task involved reading a Lucene Index from a directory, creating an inverted index from that index and then performing various functions on the built inverted index. 
The input is provided via a text file, passed as an argument to the file and the ouput is then saved in a file, whose path is also provided via the command line.

### Functions Performed
* Term at a Time AND (TaatAnd)
* Term at a Time OR (TaatOr)
* Document at a Time AND (DaatAnd)
* Document at a Time OR (DaatOr)


### Usage
> java -jar file-name.jar path-of-index output.txt input.txt
