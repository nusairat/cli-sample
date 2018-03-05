
# Wallet Hub Test
Purpose of this is to load a log file into MySQL and then checks if a given IP makes more than a certian number of requests during a given duration.

## Givens
There are mainly database givens that are needed to test the app with

### Database
The app assumes an existing database created. The existing database should have the tables:

This will store the entries for each IP.
```
 CREATE TABLE log_entry (
     ID int NOT NULL AUTO_INCREMENT,
	 entry_date DATETIME not null,
     ip VARCHAR(15) not null,
     request VARCHAR(50) not null,
     status int not null,
     user_agent VARCHAR(250) not null,
     primary key(id))
```

and

This will report all those that are blocked.
```
 CREATE TABLE report_entry (
     ID int NOT NULL AUTO_INCREMENT,
	 entry_date DATETIME not null,
     ip VARCHAR(15) not null,
     request VARCHAR(50) not null,
     status int not null,
     user_agent VARCHAR(250) not null,
     comment VARCHAR(500) not null,
     primary key(id))
```

#### Database Credentials
The Database should be loaded on `localhost` with a database name of `wallethub` and user of `wallet` with a password of `1234`.

## Running the App
The app compiles to a jar file that can be ran from the command line via:
`java -cp "parser.jar" com.ef.Parser --accesslog=/path/to/file --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100 `

The `accesslog` parameters is optional all others are required.


