# Databases

## Project description
An implementation of the Databases coursework for the Object Oriented Programming with Java module. I implemented logic so that the DBServer class can handle incoming commands from DBClient. 

To achieve this, the parser generates an Abstract Syntax Tree (AST) and then an interpreter evaluates the AST in order to generate a string to send back to the client. 

The BNF for commands can be found [here](documents/BNF.txt). An example transcript detailing some commands and their expected output can be found [here](documents/example-transcript.txt). 
