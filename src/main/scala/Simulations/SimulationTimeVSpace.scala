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

class SimulationTimeVSpace
object SimulationTimeVSpace:
  @main
  def startTimeVSpace: Unit ={

    val configTimeSim: Config = CloudSimUtils.getconfigValue("SimTimeVSpace","TimeShared")
    val configSpaceSim: Config = CloudSimUtils.getconfigValue("SimTimeVSpace","SpaceShared")

    val logger = CreateLogger(classOf[StartSimulation])
    val Timesimulation = new CloudSim()
    val spacesimulation = new CloudSim()

    val datacenter1 = new ConfiguredDataCenter(Timesimulation,configTimeSim).getConfiguredDataCenter()
    val datacenter2 = new ConfiguredDataCenter(spacesimulation,configSpaceSim).getConfiguredDataCenter()

    logger.info("Datacenter created!")

    val brokertime = new ConfiguredBroker(Timesimulation,configTimeSim).getConfiguredBroker()
    val brokerspace = new ConfiguredBroker(spacesimulation,configSpaceSim).getConfiguredBroker()

    logger.info("Broker created!")

    val vmsTime = new ConfiguredVm(Timesimulation,brokertime,configTimeSim)
    vmsTime.submitinitialVms()
    val vmsSpace = new ConfiguredVm(spacesimulation,brokerspace,configSpaceSim)
    vmsSpace.submitinitialVms()

    logger.info("Vms created as per configuration and submitted to broker")
    // Finally start a map reduce job which dynamically submits the requests to the broker

    val mapreduceConfig: Config = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")

    CloudSimUtils.configureNetwork(brokertime,List(datacenter1))

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

  }
  
