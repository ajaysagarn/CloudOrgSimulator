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
In this homework, you will experiment with creading cloud computing datacenters and running jobs on them to determine how different organizations and pricing strategies result in variabilities of cloud offerings. Of course, creating real cloud computing datacenters takes hundreds of millions of dollars and acres of land and a lot of complicated equipment, and you don't want to spend your money and resources creating physical cloud datacenters for this homework ;-). Instead, we have a cloud simulation framework, a software package that enables cloud engineers to model the cloud environments using different cloud computing models that we study in the lectures. We will use [*CloudSimPlus* simulation framework](https://cloudsimplus.org/) that is an extension of *CloudSim*, a framework and a set of libraries for modeling and simulating cloud computing infrastructure and services.

[CloudSimPlus website](https://cloudsimplus.org/) and its early predecessor [CloudSim website](http://www.cloudbus.org/cloudsim/) contain a wealth of information and it is your starting point. Your starting point is to experiment with *CloudSimPlus* and to run [examples that are provided in the corresponding Github repo](https://github.com/manoelcampos/cloudsimplus/tree/master/cloudsim-plus-examples). You should be able to convert these examples in Scala and run them using sbt command line and IntelliJ as I showed in my baseline implementation of the [minimal but complete simulation example](https://cloudsimplus.org/#example). Those who want to read more about modeling physical systems and creating simulations can find ample resources on the Internet - I recommend the following paper by [Anu Maria on Introduction to Modeling and Simulation](http://acqnotes.com/Attachments/White%20Paper%20Introduction%20to%20Modeling%20and%20Simulation%20by%20Anu%20Maria.pdf).

This homework script is written using a retroscripting technique, in which the homework outlines are generally and loosely drawn, and the individual students improvise to create the implementation that fits their refined objectives. In doing so, students are expected to stay within the basic requirements of the homework and they are free to experiments. Asking questions is important, so please ask away!

## Functionality
Once you have configured and run the minimal but complete CloudSimPlus example in this project, which should not take you more than 15 minutes, your job is to convert more cloud simulation examples into Scala and add to this project to perform more simulations where you will evaluate two or more datacenters with different characteristics (e.g., operating systems, costs, devices) and policies. Imagine that you are a cloud computing broker and you purchase computing time in bulk from different cloud providers and you sell this time to your customers, so that they can execute their jobs, i.e., cloudlets on the infrastructure of these cloud providers that have different policies and constraints. As a broker, your job is to buy the computing time cheaply and sell it at a good markup. One way to achieve it is to take cloudlets from your customers and estimate how long they will execute. Then you charge for executing cloudlets some fixed fee that represent your cost of resources summarily. Some cloudlets may execute longer than you expected, the other execute faster. If your revenue exceeds your expenses for buying the cloud computing time in bulk, you are in business, otherwise, you will go bankrupt!

There are different policies that datacenters can use for allocating Virtual Machines (VMs) to hosts, scheduling them for executions on those hosts, determining how network bandwidth is provisioned, and for scheduling cloudlets to execute on different VMs. Randomly assigning these cloudlets to different datacenters may result in situation where the executions of these cloudlets are inefficient and they takes a long time. As a result, you exhaust your supply of the purchased cloud time and you may have to refund the money to your customers, since you cannot fulfil the agreement, and you will go bankrupt. Modeling and simulating the executions of cloudlets in your clouds may help you chose a proper model for your business.

Once you experimented with the examples from *CloudSimPlus*, your next job will be to create simulations where you will evaluate a large cloud provider with many datacenters with different characteristics (e.g., operating systems, costs, devices) and policies. You will form a stream of jobs, dynamically, and feed them into your simulation. You will design your own datacenter with your own network switches and network links. You can organize cloudlets into tasks to accomplish the same job (e.g., a map reduce job where some cloudlets represent mappers and the other cloudlets represent reducers). There are different policies that datacenters can use for allocating Virtual Machines (VMs) to hosts, scheduling them for executions on those hosts, determining how network bandwidth is provisioned, and for scheduling cloudlets to execute on different VMs. Randomly assigning these cloudlets to different datacenters may result in situation where the execution is inefficient and takes a long time. Using a more clever algorithm like assigning tasks to specific clusters where the data is located may lead to more efficient cloud provider services.

Consider a snippet of the code below from one of the examples that come from the *CloudSimPlus* documentation. In it, a network cloud datacenter is created with network hardware that is used to organize hosts in a connected network. VMs can exchange packets/messages using a chosen network topology. Depending on your simulation construct, you may view different levels of performances.
```java
protected final NetworkDatacenter createDatacenter() {
  final int numberOfHosts = EdgeSwitch.PORTS * AggregateSwitch.PORTS * RootSwitch.PORTS;
  List<Host> hostList = new ArrayList<>(numberOfHosts);
  for (int i = 0; i < numberOfHosts; i++) {
      List<Pe> peList = createPEs(HOST_PES, HOST_MIPS);
      Host host = new NetworkHost(HOST_RAM, HOST_BW, HOST_STORAGE, peList)
                    .setRamProvisioner(new ResourceProvisionerSimple())
                    .setBwProvisioner(new ResourceProvisionerSimple())
                    .setVmScheduler(new VmSchedulerTimeShared());
      hostList.add(host);
  }

  NetworkDatacenter dc =
          new NetworkDatacenter(
                  simulation, hostList, new VmAllocationPolicySimple());
  dc.setSchedulingInterval(SCHEDULING_INTERVAL);
  dc.getCharacteristics()
        .setCostPerSecond(COST)
        .setCostPerMem(COST_PER_MEM)
        .setCostPerStorage(COST_PER_STORAGE)
        .setCostPerBw(COST_PER_BW);
  createNetwork(dc);
  return dc;
}

