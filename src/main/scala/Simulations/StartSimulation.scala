package Simulations

import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import HelperUtils.{CloudSimUtils, CreateLogger, ObtainConfigReference}
import MapReduce.MapReduceJob
import Simulations.BasicCloudSimPlusExample.config
import com.typesafe.config.Config
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.listeners.EventInfo

import collection.JavaConverters.*

class StartSimulation
object StartSimulation {

  @main
  def startMySumulation() = {
    val config: Config = CloudSimUtils.getconfigValue("saasDataCenter")

    val logger = CreateLogger(classOf[StartSimulation])
    val simulation = new CloudSim()
    val datacenter = new ConfiguredDataCenter(simulation,config).getConfiguredDataCenter()
    logger.info("Datacenter created!")

    val broker = new ConfiguredBroker(simulation,config).getConfiguredBroker()

    logger.info("Broker created!")

    val vms = new ConfiguredVm(simulation,broker,config)
    vms.submitinitialVms()

    logger.info("Vms created as per configuration and submitted to broker")
    // Finally start a map reduce job which dynamically submits the requests to the broker

    val mapreduceConfig: Config = CloudSimUtils.getconfigValue("MapReduceJob")

    val mapReduceJob = new MapReduceJob(simulation,broker,mapreduceConfig)

    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator.utilizationRatio"));
    val cloudletList = List.fill(10)(new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs")))

    val mapperCloudletList = mapReduceJob.getMappers()

    broker.submitCloudletList(mapReduceJob.getMappers().asJava)

    logger.info("Cloudlets submitted to broker")
    logger.info("---------Starting Simulation--------------")
    simulation.start()

    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();

    val vmLogs = vms.getVmTimeLogs

    vms.getVmTimeLogs.foreach(log => {
      logger.info(log)
    })

  }
}
