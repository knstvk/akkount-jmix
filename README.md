# akkount

A simple personal finance application built with [Jmix](https://jmix.io) framework.

![desktop-ui](img/ops-desktop.png)

<p align="center">
    <img src="img/ops-mobile.png" width="300" alt="mobile"/>
</p>


## Features

In short, the application solves two problems:
 1. It shows the current balance by all accounts: cash, credit cards, deposits, debts, etc.
 2. It can generate a report by expense and income categories that shows where the money came from and what they were spent on in some period of time.

Some details:
* There are _accounts_ that represent different kinds of money.
* There are _operations_: income to account, expense from account and transfer between accounts.
* A _category_ can be set for expense or income operations.
* The current balance is constantly displayed and is recalculated after each operation.
* Categories report shows the summary by two arbitrary periods of time to allow quick visual comparison. Any category can be excluded from the report. You can "drill down" into any row to see operations that comprise the row.
* The system has a fully functional Jmix UI and a mobile-friendly UI based on React Admin. 

## Development

You should have Java 8+, npm 7+ and Jmix Studio 1.1+ installed.

Open the project in Jmix Studio and run the application server using *Jmix Application* run/debug configuration. The application will use the HSQL database automatically created in the `.jmix/db` directory.

The main UI is available at http://localhost:8080/akk. Login as `admin` / `admin`. 

You can generate some test data:

- Open *Administration > JMX Console*, find `akkount.jmx:type=akk_SampleDataGenerator` MBean and open it.
- Enter the number of days to generate (e.g. 100) in the parameter field of the `generateSampleData` method and click *Invoke*.     

Open the terminal in the `frontend` directory and run `npm run start`. The frontend UI will be available at http://localhost:3000.


## Building and running

Open the terminal in the project directory and run the following command to build the executable JAR file:

```
./gradlew bootJar
```

The resulting JAR will be created in `build/libs` directory.

Run the application:

```
java -jar akkount-0.5.jar
```

You can also use the Bash scripts located in `etc` to start and stop the application.

The main UI is available at http://localhost:8080/akk, frontend UI at http://localhost:8080/akk/front. Username: `admin`, password: `admin`.
