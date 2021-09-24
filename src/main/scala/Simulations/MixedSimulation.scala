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

class MixedSimulation
object MixedSimulation:
  @main
  def StartMixedSimulation(): Unit ={
    val logger = CreateLogger(classOf[StartSimulation])

    val saasConfig = CloudSimUtils.getconfigValue("Saas","saasDataCenter")
    val iaasConfig = CloudSimUtils.getconfigValue("Iaas","iaasDataCenter")
    val paasConfig = CloudSimUtils.getconfigValue("paas","paasDataCenter")

    val mapreduceconfig = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")

    val simulation = new CloudSim()

    val saasDatacenter:NetworkDatacenter = new ConfiguredDataCenter(simulation,saasConfig).getConfiguredDataCenter()
    val paasDatacenter:NetworkDatacenter = new ConfiguredDataCenter(simulation,iaasConfig).getConfiguredDataCenter()
    val iaasDatacenter:NetworkDatacenter = new ConfiguredDataCenter(simulation,paasConfig).getConfiguredDataCenter()

    /**
     * These brokers represent end customer entities. Requests to specific datacenters will be sent to the respective broker
     */
    val broker = new ConfiguredBroker(simulation,saasConfig).getConfiguredBroker()


    val saasVms = new ConfiguredVm(simulation,broker,saasConfig)
    val paasVms = new ConfiguredVm(simulation,broker,paasConfig)
    val iaasvms = new ConfiguredVm(simulation,broker,iaasConfig)

    val iaasCustomerInptuts:Config = CloudSimUtils.getconfigValue("cloudCustomer","IAAS")
    val paasCustomerInptuts:Config = CloudSimUtils.getconfigValue("cloudCustomer","PAAS")

    // Make the changes to the paas datacenter according to the inputs provided by an external customer
    paasDatacenter.getCharacteristics()
      .setOs(paasCustomerInptuts.getString("OS"))
      .setArchitecture(paasCustomerInptuts.getString("ARCHITECTURE")).setVmm(paasCustomerInptuts.getString("DEFAULT_VMM"))

    // Make the changes to the saas datacenter according to the inputs provided by an external customer
    iaasDatacenter.getCharacteristics()
      .setOs(iaasCustomerInptuts.getString("OS"))
      .setArchitecture(iaasCustomerInptuts.getString("ARCHITECTURE")).setVmm(iaasCustomerInptuts.getString("DEFAULT_VMM"))

    //make the changes to the iaas datacenter according to the requests made by an external customer
    iaasvms.applyCustomerChanges(iaasCustomerInptuts)



    saasVms.submitinitialVms();
    paasVms.submitinitialVms();
    iaasvms.submitinitialVms();

    val saasmapReduceJob = new MapReduceJob(simulation,broker,mapreduceconfig,targetDC = Option.apply(saasDatacenter))
    val paasmapReduceJob = new MapReduceJob(simulation,broker,mapreduceconfig,targetDC = Option.apply(paasDatacenter))
    val iaasmapReduceJob = new MapReduceJob(simulation,broker,mapreduceconfig,targetDC = Option.apply(iaasDatacenter))

    saasmapReduceJob.submitJob()
    paasmapReduceJob.submitJob()
    iaasmapReduceJob.submitJob()

    CloudSimUtils.configureNetwork(broker, List(saasDatacenter,paasDatacenter,iaasDatacenter))
/*
    CloudSimUtils.configureNetwork(paasBroker, List(paasDatacenter))
    CloudSimUtils.configureNetwork(iaasBroker, List(iaasDatacenter))
*/

    simulation.start()

    //saasDatacenter.

    println("-------saas--------------")
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();
    println("-------paas--------------")
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();
    println("-------iaas--------------")
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();

    println("---------Total Running cost for Saas--------------")
    val saasmapperCosts = CloudSimUtils.cost1(saasDatacenter,saasmapReduceJob.getMappers())
    val saasreducerCosts = CloudSimUtils.cost1(saasDatacenter,saasmapReduceJob.getReducers())
    println(saasmapperCosts + saasreducerCosts)
    println("---------Total Running cost for Paas--------------")
    val paasmapperCosts = CloudSimUtils.cost1(paasDatacenter,paasmapReduceJob.getMappers())
    val paasreducerCosts = CloudSimUtils.cost1(paasDatacenter,paasmapReduceJob.getReducers())
    println(paasmapperCosts + paasreducerCosts)
    println("---------Total Running cost for Iaas--------------")
    val iaasmapperCosts = CloudSimUtils.cost1(iaasDatacenter,iaasmapReduceJob.getMappers())
    val iaasreducerCosts = CloudSimUtils.cost1(iaasDatacenter,iaasmapReduceJob.getReducers())
    println(iaasmapperCosts + iaasreducerCosts)
  }
