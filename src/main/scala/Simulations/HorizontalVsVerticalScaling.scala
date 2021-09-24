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

class HorizontalVsVerticalScaling
object HorizontalVsVerticalScaling:

  @main
  def startHorzontalVsVertical(): Unit ={


/*    val config: Config = CloudSimUtils.getconfigValue("VerticalVHorizontalScale","HorizontalScaling")

    val logger = CreateLogger(classOf[StartSimulation])
    val simulation = new CloudSim()
    val configDatacenter = new ConfiguredDataCenter(simulation,config)
    val datacenter = new ConfiguredDataCenter(simulation,config).getConfiguredDataCenter()
    //configDatacenter.createNetwork(datacenter)
    logger.info("Datacenter created!")

    val broker = new ConfiguredBroker(simulation,config).getConfiguredBroker()

    logger.info("Broker created!")

    val vms = new ConfiguredVm(simulation,broker,config)
    vms.submitinitialVms()

    logger.info("Vms created as per configuration and submitted to broker")
    // Finally start a map reduce job which dynamically submits the requests to the broker

    val mapreduceConfig: Config = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")

    val mapReduceJob = new MapReduceJob(simulation,broker,mapreduceConfig)

    val mapperCloudletList = mapReduceJob.getMappers()

    broker.submitCloudletList(mapReduceJob.getMappers().asJava)

    logger.info("Cloudlets submitted to broker")
    simulation.start()

    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();

    val vmLogs = vms.getVmTimeLogs
    vms.getVmTimeLogs.foreach(log => {
      logger.info(log)
    })

    val vmsCreated = broker.getVmCreatedList[NetworkVm]
    val total:CostTotals  = CloudSimUtils.VmCostMetrics(vmsCreated.asScala.toList, true)

    //print(total)

    val saasmapperCosts = CloudSimUtils.cost1(datacenter,mapReduceJob.getMappers())
    val saasreducerCosts = CloudSimUtils.cost1(datacenter,mapReduceJob.getReducers())
    println(saasmapperCosts + saasreducerCosts)*/



    val configHorizSim: Config = CloudSimUtils.getconfigValue("VerticalVHorizontalScale","HorizontalScaling")
    val configVertSim: Config = CloudSimUtils.getconfigValue("VerticalVHorizontalScale","VerticalScaling")

    val logger = CreateLogger(classOf[HorizontalVsVerticalScaling])
    val HScalesimulation = new CloudSim()
    val VScaleimulation = new CloudSim()

    val datacenter1 = new ConfiguredDataCenter(HScalesimulation,configHorizSim).getConfiguredDataCenter()
    val datacenter2 = new ConfiguredDataCenter(VScaleimulation,configVertSim).getConfiguredDataCenter()

    logger.info("Datacenter created!")

    val brokerHScale = new ConfiguredBroker(HScalesimulation,configHorizSim).getConfiguredBroker()
    val brokerVScale = new ConfiguredBroker(VScaleimulation,configVertSim).getConfiguredBroker()

    logger.info("Broker created!")

    val vmsHScale = new ConfiguredVm(HScalesimulation,brokerHScale,configHorizSim)
    vmsHScale.submitinitialVms()
    val vmsVScale = new ConfiguredVm(VScaleimulation,brokerVScale,configVertSim)
    vmsVScale.submitinitialVms()

    logger.info("Vms created as per configuration and submitted to broker")
    // Finally start a map reduce job which dynamically submits the requests to the broker

    val mapreduceConfig: Config = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")

    CloudSimUtils.configureNetwork(brokerHScale,List(datacenter1))
    CloudSimUtils.configureNetwork(brokerVScale,List(datacenter2))

    val mapReduceHscale = new MapReduceJob(HScalesimulation,brokerHScale,mapreduceConfig)
    val mapReduceVscale = new MapReduceJob(VScaleimulation,brokerVScale,mapreduceConfig)

    mapReduceHscale.submitJob()
    mapReduceVscale.submitJob()

    logger.info("Cloudlets submitted to broker")
    HScalesimulation.start()

    VScaleimulation.start()


    new CloudletsTableBuilder(brokerHScale.getCloudletFinishedList()).build();
    println("-------------------------------------------------")
    new CloudletsTableBuilder(brokerVScale.getCloudletFinishedList()).build();
    println("-------Horizontal scaling------------")
    val vmsCreatedHscale = brokerHScale.getVmCreatedList[NetworkVm]
    val totalHs:CostTotals  = CloudSimUtils.VmCostMetrics(vmsCreatedHscale.asScala.toList, true)
    CloudSimUtils.printCostTotals(totalHs)
    println("-------Vertical scaling------------")
    val vmsCreatedVscale = brokerVScale.getVmCreatedList[NetworkVm]
    val totalVs:CostTotals  = CloudSimUtils.VmCostMetrics(vmsCreatedVscale.asScala.toList, true)
    CloudSimUtils.printCostTotals(totalVs)
    
  }