# Azure-Cognitive-Search-Learn

## Prerequisite
- JDK 8+
- Maven
- Powershell
- Azure account with subscription
- Azure sql server and database along with tables having data entries
- Azure sql credentials

## Experiment
- Creating Azure Cognitive Search using Java SDK and configuration of datasources, indexes, indexers will be done in java program. Credentials and names are loaded from input.properties file.
- Creating Azure Cognitive Search using Powershell. Configuration of datasources, indexes and indexers are stored in json file then loaded from powershell, substituted using input.properties file and called.

## Basic Commands
### Azure-cli

#### Note :- If using Azure Cli authentication for java sdk, retrieve resource group name and set the subscription accordingly

- List Subscriptions
  ```
  az account list --output table
  ```
- Set Subscription
  ```
  az account set --subscription <subscription id>
  ```
- List Resource Groups
  ```
  az group list --output table
  ```

## Program Run

### Java
Use this command inside to run java code
```
mvn exec:java
```

### Powershell
Run the createCognitiveSearch.ps1 file
