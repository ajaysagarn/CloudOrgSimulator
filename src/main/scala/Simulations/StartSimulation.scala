package Simulations

import CloudSimWrappers.ConfiguredDataCenter
import HelperUtils.{CloudSimUtils, CreateLogger, ObtainConfigReference}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.CloudSim

class StartSimulation
object StartSimulation {

  @main
  def startMySumulation() = {
    val config: Config = CloudSimUtils.getconfigValue("saasDataCenter")

    val logger = CreateLogger(classOf[StartSimulation])
    val simulation = new CloudSim()
    val datacenter = new ConfiguredDataCenter(simulation,config).getConfiguredDataCenter()
    logger.info("Datacenter created!")
  }

}
