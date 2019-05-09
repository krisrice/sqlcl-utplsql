
# WIP

....This is a work in progress....

# Basic UTPLSQL command in SQLcl

This repo has a utplsql command for sqlcl. This command is `SQL>utplsql auto on|off` The command will scan all compilations for associated Test Suites then run them.

# Example

Step 1: enable.

```
SQL> set serveroutput on
SQL> 

SQL> utplsql auto on   <<<<<<<<<<<< ENABLED !!!!!!
UTPLSQL Auto enabled
```

Step 2: compile as normal
```
SQL> 
SQL> create or replace function betwnstr( a_string varchar2, a_start_pos integer, a_end_pos integer ) return varchar2
  2  is
  3  begin
  4    return substr( a_string, a_start_pos, a_end_pos - a_start_pos );
  5  end;
  6  /

Function BETWNSTR compiled
```

Step 3:  a suite is detected for this function so run it.
```
SUITE Detected. Running Suite: test_betwnstr ...
Between string function
  Returns substring from start position to end position [.008 sec] (FAILED - 1)
  Returns substring when start position is zero [.001 sec]
 
Failures:
 
  1) basic_usage
      Actual: '234' (varchar2) was expected to equal: '2345' (varchar2) 
      at "KLRICE.TEST_BETWNSTR.BASIC_USAGE", line 5 ut.expect( betwnstr( '1234567', 2, 5 ) ).to_equal('2345');
      
       
Finished in .010743 seconds
2 tests, 1 failed, 0 errored, 0 disabled, 0 warning(s)
 
SQL> 
```

## Setup

Run the `configure.sh` script included which find `sql` in the path and populates the SQLCL\_HOME,SQLCL\_BIN, and SQLCL\_VERSION to be used by the maven build process

### Maven Repository
The script will add 2 files to the local maven repository. These contain the required java APIs to interact with sqlcl.

	${SQLCL_BIN}/../lib/dbtools-common.jar
	${SQLCL_BIN}/../lib/sqlcl.jar

### sqlcl.properties
The configure script also creates a `sqlcl.properties` file which contains the following used later by maven to deploy the custom command to 

	sqlcl.bin=../sqlcl/bin
	sqlcl.home=../sqlcl
	sqlcl.version=18.4.0.0
	
	

## Build
`mvn install` this will install the code and it's dependecies into $SQLCL_HOME/lib/ext



