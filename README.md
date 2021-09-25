## Ajay Sagar Nandimandalam - Homework 1
### UIN: 659867916 
## Create cloud simulators in Scala for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.

## Running this repository

1. Clone this repository using the command
```git clone https://github.com/ajaysagarn/CloudOrgSimulator```.
2. Open the teminal within the cloned project.
3. Run tests by using the command ```sbt clean compile test```
4. Run the project itself using the command ```sbt clean compile run```
5. Select a simulation from the prompted list to run the specific simulation

You can also run this project from withi IntelliJ Idea by:
1. From IntelliJ open the cloned repository by clicking ```File > Open > Select the repository```
2. Open the integrated sbt terminal within IntelliJ and run the following commands individually
   1. ```clean   compile test``` To run all the test cases
   2. ```clean   compule    run``` To run the project
   3. Run the required simulation by selection one of the options provided.

## Overview

This respository consists of several simulations aimed at analysing various cloud computing models and how choosing certain features/factors affect the overall price incurred from using such a cloud infrastructure.
We first run a simulation to understand the usage of diferent types of Vm Scheduling and Cloudlet scheduling policies and study the cost metrics of the simulation by trying different combinations of schedulers. Next we look at
different types of dynamic scaling options i.e Vertical / Horizontal, and look at how using such scaling options can affect the performance and cost. Finally we simulate 3 types of cloud implementation models namely SAAS, PAAS and IAAS
and understand how certain pricing models and parameters selected for a model can afftect the overall costs for such a model.

## Table of Contents

   1. [Introduction](./docs/Introduction.md)
   2. [Implementaion Details](./docs/implementaionDetails.md)
   3. [Simulations](./docs/simulations.md)
