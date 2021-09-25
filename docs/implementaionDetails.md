## Implementation Details

Wrapper classes are written for the core components of cloud sim library where each class can take in a config and provide the required instance of a cloudSim entity after applying all the parameters. This enables code re-usability and also allows testing different combinations
of the cloud infrastructure by simple modifying or supplying a new config.

In all the simulations in this project use three classes ```ConfigureBroker``` ```ConfigureDataCenter``` and ```ConfigureVm``` to generate instances required of the classes according to some simulation specific configurations.

The Structure of a config file is as shown below

```conf
IAAS  {
    utilizationRatio = 0.5
    schedulingInterval = 1
    VMAllocationPolicy = BestFit #BestFit #FirstFit #Random #RoundRobin #Simple
    costPerCPUSecond = 0.03
    costPerMemory = 0.05
    costPerStorage =0.005
    costPerBW =0.005
    OS = linux
    host {
        Count = 12
        Pes = 8
        PemipsCapacity = 1000
        RAMInMBs = 10000
        StorageInMBs = 100000
        BandwidthInMBps = 10000
        VMschedulerType = SpaceShared #SpaceShared #TimeShared
    }
    vm {
        CloudletSchedulerType = SpaceShared #SpaceShared, TimeShared
        InitialCount = 4
        Pes = 4
        mipsCapacity = 1000
        RAMInMBs = 1024
        BandwidthInMBps = 1000
        StorageInMMapBs = 10000
        DefaultSubmissionDelay = 0

        HorizontalScalingEnabled = false
        HorizontalScaling {
            OverLoadThreshold = 0.8
        }

        VerticalCpuScalingEnabled = false
        VerticalCpuScaling {
            ScalingFactor = 0.1
            LowerCpuUtilizationThreshold =0.3
            UpperCpuUtilizationThreshold = 0.8
        }

        VerticalRamScalingEnabled = false
        VerticalRamScaling {
            ScalingFactor = 0.1
            LowerRamUtilizationThreshold =0.5
            UpperRamUtilizationThreshold = 0.8
        }
    }
    broker {
        BrokerType = Simple #FirstFit, Simple, BestFit
        VMDestructionDelay = 5 # by setting this paramater, the vms will be automatically downscaled after the specified idle time
    }
}
```
Here we can see that most of the parameters such as schedulers, scaling opitons, costs etc are all configurable from within this structure, using variations of which all the simulations are performed.
Util methods have been implemented to create the correct instances of CloudSimPlus classes based on the values specified within the config file.

### Map Reduce

All simulations in this repository receive a stream of cloudlets in the form a Map-reduce Job. A configuration for the map reduce jobs is as shown below

```conf
MapReduceJob  {
    TotalDataSizeMbs = 100000
    SplitSizeMbs = 1000
    #properties that define a mapper cloudlet
    MapperRamMbs = 150
    MapperBw = 100
    MapperPe = 2
    MapperSubmissionDelay = 3
    MapperUtilizationModelRam = Stochastic #Dynamic #Full #Stochastic
    MapperUtilizationModelBw = Stochastic
    MapperUtilizationModelCpu = Stochastic

    #properties that define a reducer cloudlet
    ReducerRamMbs = 150
    ReducerBw = 100
    ReducerPe = 2
    ReducerSubmissionDelay = 3
    ReducerUtilizationModelRam = Stochastic
    ReducerUtilizationModelBw = Stochastic
    ReducerUtilizationModelCpu = Stochastic

    TransferedBytes = 10
    TransferMemory = 10
}
```

This configuration accepts the total size of the map-reduce job as well as the split size for every mapper operation. The total number of mapper cloudlets generated
would depend on the total size and the split size of the map-reduce job. The default utilization model is set to Stochastic as it provides a more real world utilization value. This can be changed to Full in the Config file to create map reduce cloudlets that use the full utilization model.

A MapReduceJob class has been written that generates mapper and reducer cloudlets and also continuously listens to the execution of every mapper cloudlet. Once a Mapper cloudlet is done executing,
a corresponding reducer cloudlet is dynamically submitted to the broker with an added delay inorder to simulate network-bw/transfer delays. This class also has functionality to assign the mapper/reducer cloudlets to a
specific datacenter provided.

### Test Cases

- Unit test cases are written to test the creation of CloudSimPlus instances (datacenter, broket, Vms) in the wrapper classes as per a given configuration file.
- The MapReduce job is also covered under test cases, where tests are performed to check the creation of mapper and reducer cloudlets according to a config. The initial submission of mapper cloudlets to the broker is also covered under a test case.
