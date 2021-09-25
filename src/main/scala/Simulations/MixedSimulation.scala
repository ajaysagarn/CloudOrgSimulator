package Simulations

import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import HelperUtils.CloudSimUtils.CostTotals
import HelperUtils.{CloudSimUtils, CreateLogger}
import MapReduce.{MapReduceJob, Mapper, Reducer}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*

/**
 * Simulation to model different cloud providers such as saas, paas and iaas
 */
class MixedSimulation
object MixedSimulation:
  @main
  def StartMixedSimulation(): Unit ={
    val logger = CreateLogger(classOf[MixedSimulation])

    // fetch the corresponding config for each type of datacenter
    val saasConfig = CloudSimUtils.getconfigValue("Saas","saasDataCenter")
    val iaasConfig = CloudSimUtils.getconfigValue("Iaas","iaasDataCenter")
    val paasConfig = CloudSimUtils.getconfigValue("paas","paasDataCenter")

    //get the configuration for the map reduce job
    val mapreduceconfig = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")

    // Create an instance of cloudsim
    val simulation = new CloudSim()

    logger.info("simulation created")

    logger.info("Instantialing individual datacenters for saas, paas and iaas")
    val saasDatacenter:NetworkDatacenter = new ConfiguredDataCenter(simulation,saasConfig).getConfiguredDataCenter()
    val paasDatacenter:NetworkDatacenter = new ConfiguredDataCenter(simulation,iaasConfig).getConfiguredDataCenter()
    val iaasDatacenter:NetworkDatacenter = new ConfiguredDataCenter(simulation,paasConfig).getConfiguredDataCenter()

    /**
     * This brokers represents end customer. Requests to specific datacenters will be sent to the broker
     */
    val broker = new ConfiguredBroker(simulation,saasConfig).getConfiguredBroker()

    logger.info("Broker created for mixed simulation")

    val saasVms = new ConfiguredVm(simulation,broker,saasConfig) // vms for the saas datacenter
    val paasVms = new ConfiguredVm(simulation,broker,paasConfig) // vms for the paas datacenter
    val iaasvms = new ConfiguredVm(simulation,broker,iaasConfig) // vms for the iaas datacenter

    logger.info("configured vms created for each datacenter")

    //Accept inputs for iaas from an external customer.
    val iaasCustomerInptuts:Config = CloudSimUtils.getconfigValue("cloudCustomer","IAAS")
    //Accept inputs for iaas from an external customer.
    val paasCustomerInptuts:Config = CloudSimUtils.getconfigValue("cloudCustomer","PAAS")

    logger.info("customer inputs for iaas and paas retreived")

    // Make the changes to the paas datacenter according to the inputs provided by an external customer
    paasDatacenter.getCharacteristics()
      .setOs(paasCustomerInptuts.getString("OS"))
      .setArchitecture(paasCustomerInptuts.getString("ARCHITECTURE")).setVmm(paasCustomerInptuts.getString("DEFAULT_VMM"))

    logger.info("customer inputs for paas applied")

    // Make the changes to the saas datacenter according to the inputs provided by an external customer
    iaasDatacenter.getCharacteristics()
      .setOs(iaasCustomerInptuts.getString("OS"))
      .setArchitecture(iaasCustomerInptuts.getString("ARCHITECTURE")).setVmm(iaasCustomerInptuts.getString("DEFAULT_VMM"))

    logger.info("customer inputs for iaas applied")

    //make the changes to the iaas datacenter according to the requests made by an external customer
    iaasvms.applyCustomerChanges(iaasCustomerInptuts)

    saasVms.submitinitialVms(); //submit the vms for saas
    paasVms.submitinitialVms(); //submit the vms for paas
    iaasvms.submitinitialVms(); //submit the vms for iaas

    logger.info("All vms have been submitted")

    val saasmapReduceJob = new MapReduceJob(simulation,broker,mapreduceconfig,targetDC = Option.apply(saasDatacenter))
    val paasmapReduceJob = new MapReduceJob(simulation,broker,mapreduceconfig,targetDC = Option.apply(paasDatacenter))
    val iaasmapReduceJob = new MapReduceJob(simulation,broker,mapreduceconfig,targetDC = Option.apply(iaasDatacenter))


    logger.info("Map reduce jobs created for individual datacenter")

    // submit the cloudlets to the broker
    saasmapReduceJob.submitJob()
    paasmapReduceJob.submitJob()
    iaasmapReduceJob.submitJob()

    logger.info("All jobs submitted to the broker")

    CloudSimUtils.configureNetwork(broker, List(saasDatacenter,paasDatacenter,iaasDatacenter))

    simulation.start()


    logger.info("simulation results for saas datacenter")
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();

    logger.info("simulation results for paas datacenter")
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();

    logger.info("simulation results for iaas datacenter")
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();

    println("---------Total Running cost for Saas--------------")
    val saasmapperCosts = CloudSimUtils.cost1(saasDatacenter,saasmapReduceJob.getMappers())
    val saasreducerCosts = CloudSimUtils.cost1(saasDatacenter,saasmapReduceJob.getReducers())
    println(f"${saasmapperCosts + saasreducerCosts}%.2f$$")
    println("---------Total Running cost for Paas--------------")
    val paasmapperCosts = CloudSimUtils.cost1(paasDatacenter,paasmapReduceJob.getMappers())
    val paasreducerCosts = CloudSimUtils.cost1(paasDatacenter,paasmapReduceJob.getReducers())
    println(f"${paasmapperCosts + paasreducerCosts}%.2f$$")
    println("---------Total Running cost for Iaas--------------")
    val iaasmapperCosts = CloudSimUtils.cost1(iaasDatacenter,iaasmapReduceJob.getMappers())
    val iaasreducerCosts = CloudSimUtils.cost1(iaasDatacenter,iaasmapReduceJob.getReducers())
    println(f"${iaasmapperCosts + iaasreducerCosts}%.2f$$" )

    logger.info("simulation ended")
  }
