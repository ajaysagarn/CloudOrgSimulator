package Simulations

import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import HelperUtils.CloudSimUtils.CostTotals
import HelperUtils.{CloudSimUtils, CreateLogger, ObtainConfigReference}
import MapReduce.MapReduceJob
import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudbus.cloudsim.vms.{VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.listeners.EventInfo

import collection.JavaConverters.*

class StartSimulation

object StartSimulation {

  @main
  def startMySumulation() = {
    val config: Config = CloudSimUtils.getconfigValue("Saas","saasDataCenter")

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
    println(saasmapperCosts + saasreducerCosts)

  }


}
