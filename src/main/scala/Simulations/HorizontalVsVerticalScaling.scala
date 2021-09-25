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
 * This Simulation is used to analyze different scaling parameters of vms in a cloud infrastructure
 */
class HorizontalVsVerticalScaling
object HorizontalVsVerticalScaling:

  @main
  def startHorzontalVsVertical(): Unit ={

    val configHorizSim: Config = CloudSimUtils.getconfigValue("VerticalVHorizontalScale","HorizontalScaling")
    val configVertSim: Config = CloudSimUtils.getconfigValue("VerticalVHorizontalScale","VerticalScaling")

    val logger = CreateLogger(classOf[HorizontalVsVerticalScaling])
    logger.info("Creating Cloud Sim instance for Horizontal scaling")
    val HScalesimulation = new CloudSim()
    logger.info("Creating Cloud Sim instance for vertical scaling")
    val VScaleimulation = new CloudSim()

    logger.info("Creating datacenter for Horizontal scaling")
    val datacenter1 = new ConfiguredDataCenter(HScalesimulation,configHorizSim).getConfiguredDataCenter()
    logger.info("Creating datacenter for vertical scaling")
    val datacenter2 = new ConfiguredDataCenter(VScaleimulation,configVertSim).getConfiguredDataCenter()

    logger.info("Datacenters created!")

    val brokerHScale = new ConfiguredBroker(HScalesimulation,configHorizSim).getConfiguredBroker()
    val brokerVScale = new ConfiguredBroker(VScaleimulation,configVertSim).getConfiguredBroker()

    logger.info("Brokers created for horizontal and vertical scaling!")

    val vmsHScale = new ConfiguredVm(HScalesimulation,brokerHScale,configHorizSim)
    vmsHScale.submitinitialVms() // submit all the vms created to the broker
    val vmsVScale = new ConfiguredVm(VScaleimulation,brokerVScale,configVertSim)
    vmsVScale.submitinitialVms() // submit all the vms created to the broker

    logger.info("Vms created as per configuration and submitted to broker")
    // Finally start a map reduce job which dynamically submits the requests to the broker
    val mapreduceConfig: Config = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")

    CloudSimUtils.configureNetwork(brokerHScale,List(datacenter1)) // configure the network topology for first simulation
    CloudSimUtils.configureNetwork(brokerVScale,List(datacenter2)) // configure the network topology for second simulation

    //Creating mapReduce job instances for the two simulations
    val mapReduceHscale = new MapReduceJob(HScalesimulation,brokerHScale,mapreduceConfig)
    val mapReduceVscale = new MapReduceJob(VScaleimulation,brokerVScale,mapreduceConfig)

    mapReduceHscale.submitJob() //submit the cloudlets to the horizontal scaling simulation
    mapReduceVscale.submitJob() //submit the cloudlets to the vertical scaling simulation

    logger.info("Cloudlets submitted to broker")

    logger.info("Starting simulation for horizontal scaling")
    HScalesimulation.start()

    logger.info("Starting simulation for vertical scaling")
    VScaleimulation.start()

    logger.info("Results Obtained for Horizontal scaling sim")
    new CloudletsTableBuilder(brokerHScale.getCloudletFinishedList()).build();

    logger.info("Results Obtained for Vertical scaling sim")
    new CloudletsTableBuilder(brokerVScale.getCloudletFinishedList()).build();
    println("-------Horizontal scaling------------")
    val vmsCreatedHscale = brokerHScale.getVmCreatedList[NetworkVm]
    val totalHs:CostTotals  = CloudSimUtils.VmCostMetrics(vmsCreatedHscale.asScala.toList, true)
    CloudSimUtils.printCostTotals(totalHs)
    println("-------Vertical scaling------------")
    val vmsCreatedVscale = brokerVScale.getVmCreatedList[NetworkVm]
    val totalVs:CostTotals  = CloudSimUtils.VmCostMetrics(vmsCreatedVscale.asScala.toList, true)
    CloudSimUtils.printCostTotals(totalVs)

    logger.info("Simulation completed")
  }
