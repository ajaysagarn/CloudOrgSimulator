## Simulations

### 1. Simulation of TimeShared and SpaceShared 

This simulation can be run from the class ``Simulations.SimulationTimeVSpace``. The config file used for this simulation is ```SimTimeVSpace.conf```

#### Scheduling policies compared: 
1. VMSchedulerSpaceShared :- This scheduler allocates one or more PEs from a Host to the VMM, and doesn't allow sharing of PEs. The allocated PEs will be used until the VM finishes running. If
there is no enough free PEs as required by a VM, or whether the available PEs doesn't have enough capacity, the allocation fails.
2. VmSchedulerTimeShared :- this scheduler is also called Hypervisor,
that defines a policy to allocate one or more PEs from a PM to a VM, and allows sharing of PEs
by multiple VMs.
3. CloudletSchedulerSpaceShared :- This scheduler allocated each cloudlet to a single vm and does not allow sharing of vm resources with multiple cloudlets. Cloudlets that need scheduling will be part of a waiting list.
4. CloudletSchedulteTimeshared :- Cloudlets execute in time-shared manner in VM.the total processing capacity of the processor cores (in
MIPS) is equally divided by the applications that are using them.

Simulations were run applying multiple combinations of the above schedulers to the configuration file  and the results were collected.

#### Results: 

The output of the simulation when using Time shared scheduling for both cloudlet and vm, and using SpaceShared shared scheduling for cloudlet and vm are shown below.

![image](https://www.github.com/ajaysagarn/ClouOrgSimulator/blob/main/docs/images/TimeVSpace.PNG)

Output summary from other combinations:

|  Running Time           | VMTimeShared(s)  | VMSpaceShared(s)  |
| -------------           |:-------------:   |     :-----:       |
| CloudletTimeShared(s)   |     259          |     253           |
| CloudletSpaceShared(s)  |     53           |     50            |

|  Costs Incurred         | VMTimeShared($)  | VMSpaceShared($)  |
| -------------           |:-------------:   |     :-----:       |
| CloudletTimeShared ($)  |     628.68       |     466.99        |
| CloudletSpaceShared ($) |     624.23       |     465.19        |

#### Conclusions:




### 2. Simulation of Horizontal and Vertical scaling

This simulation can be run from the class ``Simulations.HorizontalVsVerticalScaling``. The config file used for this simulation is ```VerticalVHorizontalScale.conf```

#### Scaling methods compared:
1. Horizontal VM Scaling :- In this type od scheduling, a vm overload threshold is specified. When a certain vm's resource utilization goes beyond this threshold, a new vm will be dynamically created and added to an available host.
2. Vertical CPU Scaling :- In this type of scaling, if the cpu utilization of a vm goes above a predefined threshold, new PEs will be dynamicaaly allocated to the VM to allow more processing capacity.
3. Vertical Ram Scaling:- This is similar to verticalCpuScaling but for VMram.

The same Map Reduce job is passed to multiple simulations with different combinations of the above scaling methods and the results are collected. 

#### Results:

Simulation output with horizontal scaling Vs CPU and RAM vertical scaling enabled:

[Image Url here]

Simulation output with horizontal scaling Vs only CPU vertical scaling enabled:

[Image Url here]

Simulation output with horizontal scaling Vs only RAM vertical scaling enabled:

[Image Url here]

#### Conclusions:


### 3. Simulation of SAAS, PAAS and IAAS cloud infrastructure models

This simulation can be run from the class ``Simulations.MixedSimulation``. This simulation uses several config files ```saas.conf``` ```paas.conf``` ```iaas.conf``` that represent the default offerings and pricing criteria of the 3 types of cloud providers.

The config file ```cloudCustomer.conf``` is used to simulate the input parameters that an external customer can provide/choose for each of the cloud providors.

#### Cloud implementation models:
1. <b>SAAS</b> :- In Software as a Service, the cloud provider hosts applications and makes them available to their users over the internet. The end users have little to no control over the internal policies and data storage structure used by the cloud provider.
Thus, in this simulation, we assume an external customer would not have any control over any of the input parameters for SAAS and the default config will be used for SAAS datacenter simulation.

2. <b>PAAS</b> :- In Platform as a Service, the cloud provider takes care of most of the servers, hosts and internal structure of the cloud infrastructure. The User however still has the option to choose internal parametes such as the Operating system to be used, programming language, database etc. In this simulation the ```cloudCustomer.conf``` file takes in such parameters for a pass customer. 
3. <b>IAAS</b> :- In Infrastructure as a Service, The cloud provider offers the essential compute, storage and networking services in a pay as you go basis, that can be purchased and configured to will by any enterprise or individual customer. In such a model several parameters such a scaling behavior, bandwidths, memory etc can be configured. Here again the ```cloudCustomer.conf```  file is used to accept input parameters.

Multiple MapReduce Jobs are created and passed to the broker which then send the jobs to the respective datacenter as shown below:

[image her of the diag]



#### Results:

The Simulation is started and the total cost of running cloudlets in each of the models is generated. Here we calculate the price for each cloudlet that runs on a datacenter based on the pricing model set for that particular datacenter.

The output from running the simulation is as shown below

[ Show Simulation output image here ]

#### Conclusions:



