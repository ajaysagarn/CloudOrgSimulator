package Simulations

import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import HelperUtils.CloudSimUtils.CostTotals
import HelperUtils.{CloudSimUtils, CreateLogger}
import MapReduce.MapReduceJob
import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import collection.JavaConverters.*

/**
 * Simulations to analyze time and space shared schedulers
 */
class SimulationTimeVSpace
object SimulationTimeVSpace:
  @main
  def startTimeVSpace: Unit ={

    val logger = CreateLogger(classOf[SimulationTimeVSpace])
    //Get the configuration for the time shared simulation
    val configTimeSim: Config = CloudSimUtils.getconfigValue("SimTimeVSpace","TimeShared")
    //Get the configuration for the space shared simulation
    val configSpaceSim: Config = CloudSimUtils.getconfigValue("SimTimeVSpace","SpaceShared")

    logger.info("Configurations retreived")

    val Timesimulation = new CloudSim() // sim instance for timescheduler simulation
    val spacesimulation = new CloudSim()// sim instance for spacescheduler simulation

    // create datacenters for the simulations
    val datacenter1 = new ConfiguredDataCenter(Timesimulation,configTimeSim).getConfiguredDataCenter()
    val datacenter2 = new ConfiguredDataCenter(spacesimulation,configSpaceSim).getConfiguredDataCenter()

    logger.info("Datacenter created!")

    // create brokers for the simulation
    val brokertime = new ConfiguredBroker(Timesimulation,configTimeSim).getConfiguredBroker()
    val brokerspace = new ConfiguredBroker(spacesimulation,configSpaceSim).getConfiguredBroker()

    logger.info("Broker created!")

    // create and submit vms for the simulations
    val vmsTime = new ConfiguredVm(Timesimulation,brokertime,configTimeSim)
    vmsTime.submitinitialVms()
    val vmsSpace = new ConfiguredVm(spacesimulation,brokerspace,configSpaceSim)
    vmsSpace.submitinitialVms()

    logger.info("Vms created as per configuration and submitted to broker")
    // Finally start a map reduce job which dynamically submits the requests to the broker

    val mapreduceConfig: Config = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")

    CloudSimUtils.configureNetwork(brokertime,List(datacenter1))

    // create and submit cloudlet jobs to the simulations
    val mapReduceJobTime = new MapReduceJob(Timesimulation,brokertime,mapreduceConfig)
    val mapReduceJobSpace = new MapReduceJob(spacesimulation,brokerspace,mapreduceConfig)

    mapReduceJobTime.submitJob()
    mapReduceJobSpace.submitJob()

    logger.info("Cloudlets submitted to broker")
    Timesimulation.start()

    spacesimulation.start()

    new CloudletsTableBuilder(brokertime.getCloudletFinishedList()).build();
    println("---------------------------")
    new CloudletsTableBuilder(brokerspace.getCloudletFinishedList()).build();
    println("-------Time Shared Scheduling------------")
    val vmsCreatedHscale = brokertime.getVmCreatedList[NetworkVm]
    val totalHs:CostTotals  = CloudSimUtils.VmCostMetrics(vmsCreatedHscale.asScala.toList, true)
    CloudSimUtils.printCostTotals(totalHs)
    println("-------Space Shared Scheduling------------")
    val vmsCreatedVscale = brokerspace.getVmCreatedList[NetworkVm]
    val totalVs:CostTotals  = CloudSimUtils.VmCostMetrics(vmsCreatedVscale.asScala.toList, true)
    CloudSimUtils.printCostTotals(totalVs)
    logger.info("Simulations ended")
  }
  
